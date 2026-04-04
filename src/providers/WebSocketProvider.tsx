import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useSessionStore } from '@stores/session.store'
import { useNotificationWebSocket } from '@features/notification/hooks/useNotificationWebSocket'
import { useCallWebSocket } from '@features/call/hooks/useCallWebSocket'
import { useZegoClient } from '@features/call/hooks/useZegoClient'

interface WebSocketProviderProps {
  children: React.ReactNode
}

const AuthenticatedSubscriptions = () => {
  useNotificationWebSocket()
  useCallWebSocket()

  const { initEngine, disconnectClient } = useZegoClient()

  useEffect(() => {
    initEngine()
    return () => { disconnectClient() }
  }, [initEngine, disconnectClient])

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
