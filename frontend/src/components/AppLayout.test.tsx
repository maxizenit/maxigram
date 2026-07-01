import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { AppLayout } from './AppLayout'
import { setTokenProvider } from '../api/client'

vi.mock('../realtime/RealtimeContext', () => ({ useStompSubscription: vi.fn() }))
vi.mock('../auth/AuthContext', () => ({ useAuth: () => ({ logout: vi.fn() }) }))

const server = setupServer(
  http.get('http://localhost:8080/api/notifications', () =>
    HttpResponse.json([
      { id: 1, type: 'NEW_SUBSCRIBER', actorId: 'a', text: 'Новый подписчик', read: false, createdAt: '' },
      { id: 2, type: 'POST_LIKED', actorId: 'b', text: 'Лайк', read: false, createdAt: '' },
      { id: 3, type: 'POST_LIKED', actorId: 'c', text: 'Старый', read: true, createdAt: '' },
    ]),
  ),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('AppLayout', () => {
  it('shows the unread notifications badge in the nav', async () => {
    render(
      <MemoryRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<p>контент</p>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    // 2 unread out of 3 -> badge shows 2.
    expect(await screen.findByText('2')).toBeInTheDocument()
    expect(screen.getByText('контент')).toBeInTheDocument()
  })
})
