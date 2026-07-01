import { Client } from '@stomp/stompjs'
import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { useAuth } from '../auth/AuthContext'

function wsUrl(): string {
  const base = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'
  return `${base.replace(/^http/, 'ws')}/ws`
}

interface RealtimeState {
  client: Client | null
  connected: boolean
}

const RealtimeContext = createContext<RealtimeState>({ client: null, connected: false })

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [state, setState] = useState<RealtimeState>({ client: null, connected: false })

  // Silent renew replaces the user object every few minutes; keep the freshest token in a
  // ref so reconnects use it without tearing down a healthy connection.
  const tokenRef = useRef<string | null>(null)
  tokenRef.current = user && !user.expired ? user.access_token : null

  const userId = user && !user.expired ? (user.profile.sub as string) : null

  useEffect(() => {
    if (!userId) return
    const client = new Client({
      brokerURL: wsUrl(),
      reconnectDelay: 5000,
      beforeConnect: () => {
        client.connectHeaders = { Authorization: `Bearer ${tokenRef.current ?? ''}` }
      },
      onConnect: () => setState((s) => ({ ...s, connected: true })),
      onDisconnect: () => setState((s) => ({ ...s, connected: false })),
      onWebSocketClose: () => setState((s) => ({ ...s, connected: false })),
    })
    client.activate()
    setState({ client, connected: false })
    return () => {
      void client.deactivate()
      setState({ client: null, connected: false })
    }
    // Recreate the client only when the signed-in identity changes, not on every token renew.
  }, [userId])

  return <RealtimeContext.Provider value={state}>{children}</RealtimeContext.Provider>
}

/** Subscribes to a STOMP destination while mounted; resubscribes when the connection is (re)established. */
export function useStompSubscription<T>(destination: string | null, onMessage: (payload: T) => void) {
  const { client, connected } = useContext(RealtimeContext)
  const handler = useRef(onMessage)
  handler.current = onMessage

  useEffect(() => {
    if (!client || !connected || !destination) return
    const subscription = client.subscribe(destination, (frame) => handler.current(JSON.parse(frame.body) as T))
    return () => subscription.unsubscribe()
  }, [client, connected, destination])
}
