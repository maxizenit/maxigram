import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { notificationsApi } from '../api/notifications'
import type { Notification } from '../api/types'
import { useStompSubscription } from '../realtime/RealtimeContext'

interface NotificationsState {
  items: Notification[]
  unreadCount: number
  markRead: (id: number) => Promise<void>
}

const NotificationsContext = createContext<NotificationsState | undefined>(undefined)

/**
 * Lives at the layout level so live pushes arrive (and the unread badge updates) on any
 * screen, not only while the notifications page is open.
 */
export function NotificationsProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Notification[]>([])

  useEffect(() => {
    notificationsApi.list().then(setItems).catch(() => undefined)
  }, [])

  useStompSubscription<Notification>('/user/queue/notifications', (incoming) =>
    setItems((prev) => (prev.some((n) => n.id === incoming.id) ? prev : [incoming, ...prev])),
  )

  async function markRead(id: number) {
    await notificationsApi.markRead(id)
    setItems((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  const value: NotificationsState = {
    items,
    unreadCount: items.filter((n) => !n.read).length,
    markRead,
  }
  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>
}

export function useNotifications(): NotificationsState {
  const ctx = useContext(NotificationsContext)
  if (!ctx) throw new Error('useNotifications must be used within a NotificationsProvider')
  return ctx
}
