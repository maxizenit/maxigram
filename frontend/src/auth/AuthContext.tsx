import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import type { User } from 'oidc-client-ts'
import { userManager } from './oidc'
import { setTokenProvider } from '../api/client'

interface AuthState {
  user: User | null
  loading: boolean
  login: () => void
  logout: () => void
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setTokenProvider(() => (user && !user.expired ? user.access_token : null))
  }, [user])

  useEffect(() => {
    userManager.getUser().then((current) => {
      setUser(current)
      setLoading(false)
    })
    const onLoaded = (loaded: User) => setUser(loaded)
    const onUnloaded = () => setUser(null)
    userManager.events.addUserLoaded(onLoaded)
    userManager.events.addUserUnloaded(onUnloaded)
    return () => {
      userManager.events.removeUserLoaded(onLoaded)
      userManager.events.removeUserUnloaded(onUnloaded)
    }
  }, [])

  const value: AuthState = {
    user,
    loading,
    login: () => void userManager.signinRedirect(),
    logout: () => void userManager.signoutRedirect(),
  }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
