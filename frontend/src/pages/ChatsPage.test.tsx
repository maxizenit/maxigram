import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { ChatsPage } from './ChatsPage'
import { setTokenProvider } from '../api/client'

const server = setupServer(
  http.get('http://localhost:8080/api/chats', () =>
    HttpResponse.json([
      { id: 1, partnerId: 'bob', anonymous: false, lastMessage: 'привет', createdAt: '' },
      { id: 2, partnerId: null, anonymous: true, lastMessage: null, createdAt: '' },
    ]),
  ),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function renderPage() {
  return render(
    <MemoryRouter>
      <ChatsPage />
    </MemoryRouter>,
  )
}

describe('ChatsPage', () => {
  it('lists chats, including anonymous ones', async () => {
    renderPage()
    expect(await screen.findByText(/Аноним/)).toBeInTheDocument()
    expect(screen.getByText(/bob/)).toBeInTheDocument()
  })

  it('queues the user when no match is found', async () => {
    server.use(
      http.post('http://localhost:8080/api/matching/requests', () => HttpResponse.json({ matched: false, chatId: null })),
    )
    const user = userEvent.setup()
    renderPage()

    await user.click(screen.getByText('Найти анонимного собеседника'))

    expect(await screen.findByRole('status')).toHaveTextContent('Вы в очереди')
  })
})
