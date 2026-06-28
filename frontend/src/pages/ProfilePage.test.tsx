import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { ProfilePage } from './ProfilePage'
import { setTokenProvider } from '../api/client'

const profile = {
  id: 'u',
  firstName: 'Аня',
  lastName: 'Ли',
  birthdate: '2000-01-01',
  timezone: 'UTC',
  interests: [{ id: 1, name: 'Футбол' }],
}

const server = setupServer(
  http.get('http://localhost:8080/api/interests', () =>
    HttpResponse.json([
      { id: 1, name: 'Футбол' },
      { id: 2, name: 'Музыка' },
    ]),
  ),
  http.get('http://localhost:8080/api/profiles/me', () => HttpResponse.json(profile)),
  http.put('http://localhost:8080/api/profiles/me', () => HttpResponse.json({ ...profile, lastName: 'Ким' })),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('ProfilePage', () => {
  it('loads the existing profile into the form', async () => {
    render(<ProfilePage />)
    expect(await screen.findByLabelText('Имя')).toHaveValue('Аня')
    expect(screen.getByLabelText('Фамилия')).toHaveValue('Ли')
  })

  it('saves changes and confirms', async () => {
    const user = userEvent.setup()
    render(<ProfilePage />)
    const lastName = await screen.findByLabelText('Фамилия')

    await user.clear(lastName)
    await user.type(lastName, 'Ким')
    await user.click(screen.getByText('Сохранить'))

    expect(await screen.findByRole('status')).toHaveTextContent('Профиль сохранён')
  })

  it('shows an empty form when there is no profile yet', async () => {
    server.use(http.get('http://localhost:8080/api/profiles/me', () => new HttpResponse(null, { status: 404 })))
    render(<ProfilePage />)
    expect(await screen.findByLabelText('Имя')).toHaveValue('')
  })
})
