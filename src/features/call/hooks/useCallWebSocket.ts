import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useCallStore } from '../store/call.store'
import { useSessionStore } from '@stores/session.store'
import { CALL_WEBSOCKET_EVENTS } from '../constants/call.constants'
import type { IncomingCallPayload, CallEndedPayload } from '../types/call.types'
import toast from 'react-hot-toast'

export const useCallWebSocket = () => {
  const userId   = useSessionStore((state) => state.userId)
  const { setIncomingCall, setCallEnded, clearSession, session } = useCallStore()

  useEffect(() => {
    if (!userId) return

    const incomingTopic = `/user/${userId}/queue/${CALL_WEBSOCKET_EVENTS.INCOMING_CALL}`
    const endedTopic    = `/user/${userId}/queue/${CALL_WEBSOCKET_EVENTS.CALL_ENDED}`

    websocketService.subscribe(incomingTopic, (frame) => {
      const payload: IncomingCallPayload = JSON.parse(frame.body)

      // Bỏ qua nếu đang trong cuộc gọi khác
      if (session?.status === 'connected' || session?.status === 'outgoing') {
        toast(`${payload.callerName} đang gọi nhưng bạn đang bận`, { icon: '📵' })
        return
      }

      setIncomingCall(payload, userId, '')
    })

    websocketService.subscribe(endedTopic, (frame) => {
      const payload: CallEndedPayload = JSON.parse(frame.body)

      if (session?.callId === payload.callId) {
        setCallEnded()
        setTimeout(clearSession, 2000)
      }
    })

    return () => {
      websocketService.unsubscribe(incomingTopic)
      websocketService.unsubscribe(endedTopic)
    }
  }, [userId, session?.callId, session?.status, setIncomingCall, setCallEnded, clearSession])
}