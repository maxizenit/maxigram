import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) return <p>Загрузка…</p>
  if (!user || user.expired) return <Navigate to="/login" replace />
  return <>{children}</>
}
