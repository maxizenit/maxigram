import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { ChatConversation } from './ChatConversation'
import { setTokenProvider } from '../api/client'

vi.mock('../realtime/RealtimeContext', () => ({ useStompSubscription: vi.fn() }))
vi.mock('../auth/AuthContext', () => ({ useAuth: () => ({ user: { profile: { sub: 'me' } } }) }))

const regularChat = {
  id: 1,
  partnerId: 'bob',
  anonymous: false,
  iAgreed: false,
  partnerAgreed: false,
  closed: false,
  newChatId: null,
  createdAt: '',
}

const server = setupServer(
  http.get('http://localhost:8080/api/chats/1', () => HttpResponse.json(regularChat)),
  http.get('http://localhost:8080/api/chats/1/messages', () =>
    HttpResponse.json([{ id: 10, chatId: 1, senderId: 'bob', text: 'привет', createdAt: '', read: true }]),
  ),
  http.post('http://localhost:8080/api/chats/1/messages', () =>
    HttpResponse.json({ id: 11, chatId: 1, senderId: 'me', text: 'ответ', createdAt: '', read: false }),
  ),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function renderAt(id: string) {
  return render(
    <MemoryRouter initialEntries={[`/chats/${id}`]}>
      <Routes>
        <Route path="/chats/:id" element={<ChatConversation />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ChatConversation', () => {
  it('shows the conversation and sends a message', async () => {
    const user = userEvent.setup()
    renderAt('1')

    expect(await screen.findByText('привет')).toBeInTheDocument()
    await user.type(screen.getByLabelText('Сообщение'), 'ответ')
    await user.click(screen.getByText('Отправить'))

    expect(await screen.findByText('ответ')).toBeInTheDocument()
  })

  it('disables sending in a closed anonymous chat (fixes the v1 inverted button)', async () => {
    server.use(
      http.get('http://localhost:8080/api/chats/2', () =>
        HttpResponse.json({ ...regularChat, id: 2, partnerId: null, anonymous: true, closed: true }),
      ),
      http.get('http://localhost:8080/api/chats/2/messages', () => HttpResponse.json([])),
    )
    renderAt('2')

    expect(await screen.findByText('Анонимный чат')).toBeInTheDocument()
    expect(screen.getByLabelText('Сообщение')).toBeDisabled()
    expect(screen.getByText('Чат закрыт')).toBeDisabled()
  })

  it('submits de-anonymization consent', async () => {
    server.use(
      http.get('http://localhost:8080/api/chats/3', () =>
        HttpResponse.json({ ...regularChat, id: 3, partnerId: null, anonymous: true }),
      ),
      http.get('http://localhost:8080/api/chats/3/messages', () => HttpResponse.json([])),
      http.post('http://localhost:8080/api/chats/3/agreement', () =>
        HttpResponse.json({ ...regularChat, id: 3, partnerId: null, anonymous: true, iAgreed: true }),
      ),
    )
    const user = userEvent.setup()
    renderAt('3')

    await user.click(await screen.findByText('Согласиться на деанонимизацию'))

    expect(await screen.findByText('Согласие подано')).toBeInTheDocument()
  })
})
