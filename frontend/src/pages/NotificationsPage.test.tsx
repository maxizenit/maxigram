import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { NotificationsPage } from './NotificationsPage'
import { setTokenProvider } from '../api/client'

vi.mock('../realtime/RealtimeContext', () => ({ useStompSubscription: vi.fn() }))

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

describe('NotificationsPage', () => {
  it('lists notifications and marks one as read', async () => {
    const user = userEvent.setup()
    render(<NotificationsPage />)

    expect(await screen.findByText('Новый подписчик')).toBeInTheDocument()
    await user.click(screen.getByText('Прочитано'))

    await waitFor(() => expect(screen.queryByText('Прочитано')).not.toBeInTheDocument())
  })
})
