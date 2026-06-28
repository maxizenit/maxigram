import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { UserProfilePage } from './UserProfilePage'
import { setTokenProvider } from '../api/client'

const profile = {
  id: 'bob',
  firstName: 'Боб',
  lastName: 'Тестов',
  birthdate: '1990-01-01',
  timezone: 'UTC',
  interests: [{ id: 1, name: 'Футбол' }],
}

const server = setupServer(
  http.get('http://localhost:8080/api/profiles/bob', () => HttpResponse.json(profile)),
  http.get('http://localhost:8080/api/subscriptions', () => HttpResponse.json([])),
  http.post('http://localhost:8080/api/subscriptions/bob', () => new HttpResponse(null, { status: 204 })),
  http.delete('http://localhost:8080/api/subscriptions/bob', () => new HttpResponse(null, { status: 204 })),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function renderAt(id = 'bob') {
  return render(
    <MemoryRouter initialEntries={[`/profiles/${id}`]}>
      <Routes>
        <Route path="/profiles/:id" element={<UserProfilePage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('UserProfilePage', () => {
  it('shows another user profile and lets you subscribe', async () => {
    const user = userEvent.setup()
    renderAt()

    expect(await screen.findByRole('heading', { name: 'Боб Тестов' })).toBeInTheDocument()
    expect(screen.getByText(/Футбол/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Подписаться' }))
    expect(await screen.findByRole('button', { name: 'Отписаться' })).toBeInTheDocument()
  })

  it('reflects an already-subscribed state', async () => {
    server.use(http.get('http://localhost:8080/api/subscriptions', () => HttpResponse.json(['bob'])))
    renderAt()

    expect(await screen.findByRole('button', { name: 'Отписаться' })).toBeInTheDocument()
  })
})
