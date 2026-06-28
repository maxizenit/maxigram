import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { ChatConversation } from './ChatConversation'
import { setTokenProvider } from '../api/client'

// Capture the subscription handler so a test can simulate an incoming STOMP frame.
const realtime = vi.hoisted(() => ({ handler: null as null | ((message: unknown) => void) }))
vi.mock('../realtime/RealtimeContext', () => ({
  useStompSubscription: (_destination: string | null, handler: (message: unknown) => void) => {
    realtime.handler = handler
  },
}))
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
  http.get('http://localhost:8080/api/profiles/bob', () =>
    HttpResponse.json({ id: 'bob', firstName: 'Боб', lastName: 'Тестов', birthdate: '1990-01-01', timezone: 'UTC', interests: [] }),
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

  it('shows the partner name as a link to their profile in a regular chat', async () => {
    renderAt('1')

    const link = await screen.findByRole('link', { name: 'Боб Тестов' })
    expect(link).toHaveAttribute('href', '/profiles/bob')
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

  it('keeps an own message as "Вы" even when the masked broadcast echoes it first (anonymous race)', async () => {
    server.use(
      http.get('http://localhost:8080/api/chats/4', () =>
        HttpResponse.json({ ...regularChat, id: 4, partnerId: null, anonymous: true }),
      ),
      http.get('http://localhost:8080/api/chats/4/messages', () => HttpResponse.json([])),
      http.post('http://localhost:8080/api/chats/4/messages', () =>
        HttpResponse.json({ id: 50, chatId: 4, senderId: 'me', text: 'моё сообщение', createdAt: '', read: false }),
      ),
    )
    const user = userEvent.setup()
    renderAt('4')
    await screen.findByText('Анонимный чат')

    // Masked broadcast (senderId=null) lands first -> would show as "Собеседник".
    act(() =>
      realtime.handler?.({ id: 50, chatId: 4, senderId: null, text: 'моё сообщение', createdAt: '', read: false }),
    )
    expect(screen.getByText('Собеседник:')).toBeInTheDocument()

    // Sending the same message reconciles it to our self-attributed copy.
    await user.type(screen.getByLabelText('Сообщение'), 'моё сообщение')
    await user.click(screen.getByText('Отправить'))

    expect(await screen.findByText('Вы:')).toBeInTheDocument()
    expect(screen.queryByText('Собеседник:')).not.toBeInTheDocument()
    expect(screen.getAllByText('моё сообщение')).toHaveLength(1)
  })
})
