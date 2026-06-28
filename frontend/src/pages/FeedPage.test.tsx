import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { FeedPage } from './FeedPage'
import { setTokenProvider } from '../api/client'

const server = setupServer(
  http.get('http://localhost:8080/api/feed', () =>
    HttpResponse.json([
      { id: 1, authorId: 'a', text: 'привет из ленты', createdAt: '', likesCount: 3, commentsCount: 1, likedByMe: false },
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

  it('publishes a new post and shows it at the top', async () => {
    server.use(
      http.post('http://localhost:8080/api/posts', () =>
        HttpResponse.json({ id: 9, authorId: 'me', text: 'свежий пост', createdAt: '', likesCount: 0, commentsCount: 0, likedByMe: false }),
      ),
    )
    const user = userEvent.setup()
    render(<FeedPage />)
    await screen.findByText(/привет из ленты/)

    await user.type(screen.getByLabelText('Новый пост'), 'свежий пост')
    await user.click(screen.getByText('Опубликовать'))

    expect(await screen.findByText('свежий пост')).toBeInTheDocument()
  })

  it('shows an error when the request fails', async () => {
    server.use(http.get('http://localhost:8080/api/feed', () => new HttpResponse(null, { status: 500 })))
    render(<FeedPage />)
    expect(await screen.findByRole('alert')).toHaveTextContent('Не удалось загрузить ленту')
  })
})
