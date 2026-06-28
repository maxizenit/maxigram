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
        </nav>
        <button onClick={logout}>Выйти</button>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
