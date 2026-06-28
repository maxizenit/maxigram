import { useEffect, useState, type FormEvent } from 'react'
import { feedApi } from '../api/feed'
import type { Post } from '../api/types'
import { PostCard } from '../components/PostCard'

export function FeedPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [error, setError] = useState<string | null>(null)
  const [text, setText] = useState('')

  useEffect(() => {
    feedApi
      .feed()
      .then(setPosts)
      .catch(() => setError('Не удалось загрузить ленту'))
  }, [])

  async function createPost(event: FormEvent) {
    event.preventDefault()
    if (!text.trim()) return
    const created = await feedApi.createPost(text)
    setPosts([created, ...posts])
    setText('')
  }

  return (
    <section>
      <h1>Лента</h1>
      <form onSubmit={createPost}>
        <textarea
          value={text}
          onChange={(event) => setText(event.target.value)}
          placeholder="Что нового?"
          aria-label="Новый пост"
        />
        <button type="submit">Опубликовать</button>
      </form>
      {error && <p role="alert">{error}</p>}
      {posts.length === 0 ? <p>Пока пусто.</p> : posts.map((post) => <PostCard key={post.id} post={post} />)}
    </section>
  )
}
