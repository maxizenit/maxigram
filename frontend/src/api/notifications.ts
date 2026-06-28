import { api } from './client'
import type { Notification } from './types'

export const notificationsApi = {
  list: () => api.get<Notification[]>('/api/notifications'),
  markRead: (id: number) => api.post<void>(`/api/notifications/${id}/read`),
}
