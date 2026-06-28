import { api } from './client'

export interface Restraint {
  startTime: string
  endTime: string
}

export const wellbeingApi = {
  get: () => api.get<Restraint | null>('/api/wellbeing/restraint'),
  set: (startTime: string, endTime: string) =>
    api.put<Restraint>('/api/wellbeing/restraint', { startTime, endTime }),
  remove: () => api.del<void>('/api/wellbeing/restraint'),
}
