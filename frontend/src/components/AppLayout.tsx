import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { NotificationsProvider, useNotifications } from '../notifications/NotificationsContext'

function NotificationsLink() {
  const { unreadCount } = useNotifications()
  return (
    <NavLink to="/notifications">
      Уведомления
      {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
    </NavLink>
  )
}

function Shell() {
  const { logout } = useAuth()
  return (
    <div>
      <header className="topbar">
        <strong>maxigram</strong>
        <nav>
          <NavLink to="/">Лента</NavLink>
          <NavLink to="/chats">Чаты</NavLink>
          <NotificationsLink />
          <NavLink to="/wellbeing">Самоограничение</NavLink>
          <NavLink to="/profile">Профиль</NavLink>
        </nav>
        <button onClick={logout}>Выйти</button>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}

export function AppLayout() {
  return (
    <NotificationsProvider>
      <Shell />
    </NotificationsProvider>
  )
}
