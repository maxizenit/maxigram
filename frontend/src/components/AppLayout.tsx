import { useEffect, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { setLockedHandler } from '../api/client'
import { wellbeingApi } from '../api/wellbeing'
import { useAuth } from '../auth/AuthContext'
import { NotificationsProvider, useNotifications } from '../notifications/NotificationsContext'

/** Shown when the API starts answering 423: explains the self-restraint instead of generic errors. */
function RestraintBanner() {
  const [until, setUntil] = useState<string | null>(null)

  useEffect(() => {
    setLockedHandler(() => {
      // The wellbeing endpoints are excluded from the block, so we can fetch the window.
      wellbeingApi
        .get()
        .then((restraint) => setUntil(restraint ? new Date(restraint.endTime).toLocaleString() : ''))
        .catch(() => setUntil(''))
    })
    return () => setLockedHandler(null)
  }, [])

  if (until === null) return null
  return (
    <p className="restraint-banner" role="alert">
      ⏳ Действует режим самоограничения{until ? ` до ${until}` : ''} — доступ к приложению заблокирован.
    </p>
  )
}

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
          <NavLink to="/people">Люди</NavLink>
          <NavLink to="/chats">Чаты</NavLink>
          <NotificationsLink />
          <NavLink to="/wellbeing">Самоограничение</NavLink>
          <NavLink to="/profile">Профиль</NavLink>
        </nav>
        <button onClick={logout}>Выйти</button>
      </header>
      <main>
        <RestraintBanner />
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
