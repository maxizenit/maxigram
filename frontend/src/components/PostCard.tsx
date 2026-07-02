import { useState, type FormEvent } from 'react'
import type { Comment, Post } from '../api/types'
import { feedApi } from '../api/feed'

export function PostCard({ post: initial }: { post: Post }) {
  const [post, setPost] = useState(initial)
  const [comments, setComments] = useState<Comment[] | null>(null)
  const [commentText, setCommentText] = useState('')

  async function toggleLike() {
    if (post.likedByMe) {
      await feedApi.unlikePost(post.id)
      setPost({ ...post, likedByMe: false, likesCount: post.likesCount - 1 })
    } else {
      await feedApi.likePost(post.id)
      setPost({ ...post, likedByMe: true, likesCount: post.likesCount + 1 })
    }
  }

  async function loadComments() {
    setComments(await feedApi.comments(post.id))
  }

  async function submitComment(event: FormEvent) {
    event.preventDefault()
    if (!commentText.trim()) return
    const created = await feedApi.addComment(post.id, commentText)
    setComments([...(comments ?? []), created])
    setCommentText('')
    setPost({ ...post, commentsCount: post.commentsCount + 1 })
  }

  async function toggleCommentLike(comment: Comment) {
    if (comment.likedByMe) {
      await feedApi.unlikeComment(comment.id)
    } else {
      await feedApi.likeComment(comment.id)
    }
    setComments(
      (prev) =>
        prev?.map((c) =>
          c.id === comment.id
            ? { ...c, likedByMe: !c.likedByMe, likesCount: c.likesCount + (c.likedByMe ? -1 : 1) }
            : c,
        ) ?? prev,
    )
  }

  return (
    <article className="card">
      <p>{post.text}</p>
      <div className="actions">
        <button onClick={toggleLike} aria-pressed={post.likedByMe} aria-label="Нравится">
          {post.likedByMe ? '♥' : '♡'} {post.likesCount}
        </button>
        <button onClick={loadComments}>💬 {post.commentsCount}</button>
      </div>
      {comments && (
        <div>
          <ul>
            {comments.map((comment) => (
              <li key={comment.id}>
                {comment.text}{' '}
                <button
                  onClick={() => toggleCommentLike(comment)}
                  aria-pressed={comment.likedByMe}
                  aria-label={`Нравится комментарий ${comment.id}`}
                >
                  {comment.likedByMe ? '♥' : '♡'} {comment.likesCount}
                </button>
              </li>
            ))}
          </ul>
          <form onSubmit={submitComment}>
            <input
              value={commentText}
              onChange={(event) => setCommentText(event.target.value)}
              placeholder="Комментарий"
              aria-label="Комментарий"
            />
            <button type="submit">Отправить</button>
          </form>
        </div>
      )}
    </article>
  )
}
