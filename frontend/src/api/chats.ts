import { api } from './client'
import type { Chat, ChatSummary, Message } from './types'

export const chatsApi = {
  list: () => api.get<ChatSummary[]>('/api/chats'),
  open: (participantId: string) => api.post<Chat>('/api/chats', { participantId }),
  get: (id: number) => api.get<Chat>(`/api/chats/${id}`),
  messages: (id: number) => api.get<Message[]>(`/api/chats/${id}/messages`),
  send: (id: number, text: string) => api.post<Message>(`/api/chats/${id}/messages`, { text }),
  agree: (id: number) => api.post<Chat>(`/api/chats/${id}/agreement`),
  close: (id: number) => api.post<Chat>(`/api/chats/${id}/close`),
}
