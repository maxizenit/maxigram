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

  useEffect(() => {
    if (!user || user.expired) return
    const client = new Client({
      brokerURL: wsUrl(),
      connectHeaders: { Authorization: `Bearer ${user.access_token}` },
      reconnectDelay: 5000,
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
  }, [user])

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
