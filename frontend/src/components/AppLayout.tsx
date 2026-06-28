import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppLayout() {
  const { logout } = useAuth()
  return (
    <div>
      <header>
        <strong>maxigram</strong>
        <nav>
          <NavLink to="/">Лента</NavLink>
          <NavLink to="/chats">Чаты</NavLink>
          <NavLink to="/notifications">Уведомления</NavLink>
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
