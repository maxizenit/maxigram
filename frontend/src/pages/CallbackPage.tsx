import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { userManager } from '../auth/oidc'

export function CallbackPage() {
  const navigate = useNavigate()
  useEffect(() => {
    userManager
      .signinRedirectCallback()
      .then(() => navigate('/', { replace: true }))
      .catch(() => navigate('/login', { replace: true }))
  }, [navigate])
  return <p>Входим…</p>
}
