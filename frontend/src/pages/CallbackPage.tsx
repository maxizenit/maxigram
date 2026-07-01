import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { userManager } from '../auth/oidc'

export function CallbackPage() {
  const navigate = useNavigate()
  // StrictMode double-invokes effects; a second signinRedirectCallback would fail on the
  // already-consumed code and its catch would override the successful navigation.
  const started = useRef(false)
  useEffect(() => {
    if (started.current) return
    started.current = true
    userManager
      .signinRedirectCallback()
      .then(() => navigate('/', { replace: true }))
      .catch(() => navigate('/login', { replace: true }))
  }, [navigate])
  return <p>Входим…</p>
}
