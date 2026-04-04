import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useSessionStore } from '@stores/session.store'
import { useNotificationWebSocket } from '@features/notification/hooks/useNotificationWebSocket'

interface WebSocketProviderProps {
  children: React.ReactNode
}

const AuthenticatedSubscriptions = () => {
  useNotificationWebSocket()
  return null
}

export const WebSocketProvider = ({ children }: WebSocketProviderProps) => {
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated)

  useEffect(() => {
    if (!isAuthenticated) return
    websocketService.connect()
    return () => { websocketService.disconnect() }
  }, [isAuthenticated])

  return (
    <>
      {isAuthenticated && <AuthenticatedSubscriptions />}
      {children}
    </>
  )
}
