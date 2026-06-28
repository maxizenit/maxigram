import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  return (
    <main>
      <h1>maxigram</h1>
      <p>Войдите, чтобы продолжить.</p>
      <button onClick={login}>Войти</button>
    </main>
  )
}
