import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
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

describe('ChatsPage', () => {
  it('lists chats, including anonymous ones', async () => {
    render(
      <MemoryRouter>
        <ChatsPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText(/Аноним/)).toBeInTheDocument()
    expect(screen.getByText(/bob/)).toBeInTheDocument()
  })
})
