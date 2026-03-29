import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useSessionStore } from '@stores/session.store'
import { useNotificationWebSocket } from '@features/notification/hooks/useNotificationWebSocket'
import { useCallWebSocket } from '@features/call/hooks/useCallWebSocket'
import { useStringeeClient } from '@features/call/hooks/useStringeeClient'

interface WebSocketProviderProps {
  children: React.ReactNode
}

const AuthenticatedSubscriptions = () => {
  useNotificationWebSocket()
  useCallWebSocket()

  const { initClient, disconnectClient } = useStringeeClient()

  useEffect(() => {
    initClient()
    return () => disconnectClient()
  }, [initClient, disconnectClient])

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