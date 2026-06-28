import { api } from './client'
import type { Comment, Post } from './types'

export const feedApi = {
  feed: () => api.get<Post[]>('/api/feed'),
  createPost: (text: string) => api.post<Post>('/api/posts', { text }),
  getPost: (id: number) => api.get<Post>(`/api/posts/${id}`),
  likePost: (id: number) => api.post<void>(`/api/posts/${id}/likes`),
  unlikePost: (id: number) => api.del<void>(`/api/posts/${id}/likes`),
  comments: (postId: number) => api.get<Comment[]>(`/api/posts/${postId}/comments`),
  addComment: (postId: number, text: string) => api.post<Comment>(`/api/posts/${postId}/comments`, { text }),
}
