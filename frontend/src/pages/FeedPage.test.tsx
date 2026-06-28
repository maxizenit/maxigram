import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { FeedPage } from './FeedPage'
import { setTokenProvider } from '../api/client'

const server = setupServer(
  http.get('http://localhost:8080/api/feed', () =>
    HttpResponse.json([
      { id: 1, authorId: 'a', text: 'привет из ленты', likesCount: 3, commentsCount: 1, likedByMe: false },
    ]),
  ),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('FeedPage', () => {
  it('renders posts loaded from the API', async () => {
    render(<FeedPage />)
    expect(await screen.findByText(/привет из ленты/)).toBeInTheDocument()
  })

  it('shows an error when the request fails', async () => {
    server.use(http.get('http://localhost:8080/api/feed', () => new HttpResponse(null, { status: 500 })))
    render(<FeedPage />)
    expect(await screen.findByRole('alert')).toHaveTextContent('Не удалось загрузить ленту')
  })
})
