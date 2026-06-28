import { useEffect, useState } from 'react'
import { api } from '../api/client'

interface Post {
  id: number
  authorId: string
  text: string
  likesCount: number
  commentsCount: number
  likedByMe: boolean
}

export function FeedPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<Post[]>('/api/feed')
      .then(setPosts)
      .catch(() => setError('Не удалось загрузить ленту'))
  }, [])

  if (error) return <p role="alert">{error}</p>

  return (
    <section>
      <h1>Лента</h1>
      {posts.length === 0 ? (
        <p>Пока пусто.</p>
      ) : (
        <ul>
          {posts.map((post) => (
            <li key={post.id}>
              {post.text} — ♥ {post.likesCount}, 💬 {post.commentsCount}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
