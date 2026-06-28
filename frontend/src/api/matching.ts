import { api } from './client'

export interface MatchResponse {
  matched: boolean
  chatId: number | null
}

export const matchingApi = {
  request: () => api.post<MatchResponse>('/api/matching/requests'),
  leave: () => api.del<void>('/api/matching/requests'),
}
