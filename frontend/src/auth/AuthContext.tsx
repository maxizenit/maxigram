import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import type { User } from 'oidc-client-ts'
import { userManager } from './oidc'
import { setTokenProvider, setUnauthorizedHandler } from '../api/client'

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
    // Fires only when the automatic silent renew failed to refresh in time:
    // drop the session so ProtectedRoute sends the user back to the login screen.
    const onExpired = () => setUser(null)
    userManager.events.addUserLoaded(onLoaded)
    userManager.events.addUserUnloaded(onUnloaded)
    userManager.events.addAccessTokenExpired(onExpired)
    // A 401 with a live-looking token means the backend rejected it (e.g. app restarted
    // with new signing keys): force a full interactive re-login.
    setUnauthorizedHandler(() => void userManager.signinRedirect())
    return () => {
      userManager.events.removeUserLoaded(onLoaded)
      userManager.events.removeUserUnloaded(onUnloaded)
      userManager.events.removeAccessTokenExpired(onExpired)
      setUnauthorizedHandler(null)
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
