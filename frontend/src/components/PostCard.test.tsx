import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { PostCard } from './PostCard'
import { setTokenProvider } from '../api/client'
import type { Post } from '../api/types'

const post: Post = {
  id: 1,
  authorId: 'a',
  text: 'тестовый пост',
  createdAt: '2026-06-28T10:00:00Z',
  likesCount: 2,
  commentsCount: 1,
  likedByMe: false,
}

const server = setupServer(
  http.post('http://localhost:8080/api/posts/1/likes', () => new HttpResponse(null, { status: 204 })),
  http.delete('http://localhost:8080/api/posts/1/likes', () => new HttpResponse(null, { status: 204 })),
  http.get('http://localhost:8080/api/posts/1/comments', () =>
    HttpResponse.json([
      { id: 5, postId: 1, authorId: 'b', text: 'первый коммент', createdAt: '', likesCount: 0, likedByMe: false },
    ]),
  ),
  http.post('http://localhost:8080/api/posts/1/comments', () =>
    HttpResponse.json({ id: 6, postId: 1, authorId: 'me', text: 'мой коммент', createdAt: '', likesCount: 0, likedByMe: false }),
  ),
)

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('PostCard', () => {
  it('toggles a like and updates the count', async () => {
    const user = userEvent.setup()
    render(<PostCard post={post} />)

    const likeButton = screen.getByLabelText('Нравится')
    expect(likeButton).toHaveAttribute('aria-pressed', 'false')

    await user.click(likeButton)

    expect(likeButton).toHaveTextContent('♥ 3')
    expect(likeButton).toHaveAttribute('aria-pressed', 'true')
  })

  it('loads comments and posts a new one', async () => {
    const user = userEvent.setup()
    render(<PostCard post={post} />)

    await user.click(screen.getByText(/💬/))
    expect(await screen.findByText('первый коммент')).toBeInTheDocument()

    await user.type(screen.getByLabelText('Комментарий'), 'мой коммент')
    await user.click(screen.getByText('Отправить'))

    expect(await screen.findByText('мой коммент')).toBeInTheDocument()
  })
})
