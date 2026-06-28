import { api } from './client'
import type { Interest, Profile, ProfileInput } from './types'

export const profileApi = {
  me: () => api.get<Profile>('/api/profiles/me'),
  byId: (id: string) => api.get<Profile>(`/api/profiles/${id}`),
  save: (input: ProfileInput) => api.put<Profile>('/api/profiles/me', input),
  interests: () => api.get<Interest[]>('/api/interests'),
  subscriptions: () => api.get<string[]>('/api/subscriptions'),
  subscribe: (authorId: string) => api.post<void>(`/api/subscriptions/${authorId}`),
  unsubscribe: (authorId: string) => api.del<void>(`/api/subscriptions/${authorId}`),
}
