import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { NotificationsPage } from './NotificationsPage'
import { NotificationsProvider } from '../notifications/NotificationsContext'
import { setTokenProvider } from '../api/client'

// Capture the queue handler so tests can push a live notification.
const realtime = vi.hoisted(() => ({ handlers: {} as Record<string, (message: unknown) => void> }))
vi.mock('../realtime/RealtimeContext', () => ({
  useStompSubscription: (destination: string | null, handler: (message: unknown) => void) => {
    if (destination) realtime.handlers[destination] = handler
  },
}))

const server = setupServer(
  http.get('http://localhost:8080/api/notifications', () =>
    HttpResponse.json([
      { id: 1, type: 'NEW_SUBSCRIBER', actorId: 'a', text: 'Новый подписчик', read: false, createdAt: '' },
    ]),
  ),
  http.post('http://localhost:8080/api/notifications/1/read', () => new HttpResponse(null, { status: 204 })),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function renderPage() {
  return render(
    <NotificationsProvider>
      <NotificationsPage />
    </NotificationsProvider>,
  )
}

describe('NotificationsPage', () => {
  it('lists notifications and marks one as read', async () => {
    const user = userEvent.setup()
    renderPage()

    expect(await screen.findByText('Новый подписчик')).toBeInTheDocument()
    await user.click(screen.getByText('Прочитано'))

    await waitFor(() => expect(screen.queryByText('Прочитано')).not.toBeInTheDocument())
  })

  it('prepends a live pushed notification', async () => {
    renderPage()
    await screen.findByText('Новый подписчик')

    act(() =>
      realtime.handlers['/user/queue/notifications']?.({
        id: 2,
        type: 'POST_LIKED',
        actorId: 'b',
        text: 'Ваш пост лайкнули',
        read: false,
        createdAt: '',
      }),
    )

    expect(await screen.findByText('Ваш пост лайкнули')).toBeInTheDocument()
  })
})
