import { useNotifications } from '../notifications/NotificationsContext'

export function NotificationsPage() {
  const { items, markRead } = useNotifications()

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
