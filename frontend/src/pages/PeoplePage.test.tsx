import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { PeoplePage } from './PeoplePage'
import { setTokenProvider } from '../api/client'

const bob = { id: 'bob', firstName: 'Боб', lastName: 'Тестов', birthdate: '1990-01-01', timezone: 'UTC', interests: [] }
const eva = { id: 'eva', firstName: 'Ева', lastName: 'Иванова', birthdate: '1995-05-05', timezone: 'UTC', interests: [] }

const server = setupServer(
  http.get('http://localhost:8080/api/subscriptions', () => HttpResponse.json(['bob'])),
  http.get('http://localhost:8080/api/profiles', ({ request }) => {
    const url = new URL(request.url)
    if (url.searchParams.get('ids') === 'bob') return HttpResponse.json([bob])
    if (url.searchParams.get('query') === 'ева') return HttpResponse.json([eva])
    return HttpResponse.json([])
  }),
  http.delete('http://localhost:8080/api/subscriptions/bob', () => new HttpResponse(null, { status: 204 })),
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
      <PeoplePage />
    </MemoryRouter>,
  )
}

describe('PeoplePage', () => {
  it('searches people by name and links to their profiles', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.type(screen.getByLabelText('Поиск людей'), 'ева')
    await user.click(screen.getByText('Найти'))

    const link = await screen.findByRole('link', { name: 'Ева Иванова' })
    expect(link).toHaveAttribute('href', '/profiles/eva')
  })

  it('lists my subscriptions with resolved names and unfollows', async () => {
    const user = userEvent.setup()
    renderPage()

    expect(await screen.findByRole('link', { name: 'Боб Тестов' })).toBeInTheDocument()

    await user.click(screen.getByText('Отписаться'))

    await waitFor(() => expect(screen.queryByRole('link', { name: 'Боб Тестов' })).not.toBeInTheDocument())
    expect(screen.getByText('Вы пока ни на кого не подписаны.')).toBeInTheDocument()
  })
})
