import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()

  // Already signed in (e.g. raced navigation after the OIDC callback): go home.
  useEffect(() => {
    if (user && !user.expired) navigate('/', { replace: true })
  }, [user, navigate])

  return (
    <main className="login">
      <h1>maxigram</h1>
      <p>Войдите, чтобы продолжить.</p>
      <button onClick={login}>Войти</button>
    </main>
  )
}
