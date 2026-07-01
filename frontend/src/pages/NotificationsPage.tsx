import { useEffect, useState } from 'react'
import { notificationsApi } from '../api/notifications'
import type { Notification } from '../api/types'
import { useStompSubscription } from '../realtime/RealtimeContext'

export function NotificationsPage() {
  const [items, setItems] = useState<Notification[]>([])

  useEffect(() => {
    notificationsApi.list().then(setItems).catch(() => undefined)
  }, [])

  // Live push from the per-user queue.
  useStompSubscription<Notification>('/user/queue/notifications', (incoming) =>
    setItems((prev) => [incoming, ...prev]),
  )

  async function markRead(id: number) {
    await notificationsApi.markRead(id)
    setItems((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  return (
    <section>
      <h1>Уведомления</h1>
      {items.length === 0 ? (
        <p>Нет уведомлений.</p>
      ) : (
        <ul className="item-list">
          {items.map((notification) => (
            <li key={notification.id}>
              {notification.text}
              {!notification.read && <button onClick={() => markRead(notification.id)}>Прочитано</button>}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
