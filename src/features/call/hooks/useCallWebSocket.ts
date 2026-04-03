import { useEffect, useRef } from 'react'
import { websocketService } from '@services/websocket.service'
import { useCallStore } from '../store/call.store'
import { useSessionStore } from '@stores/session.store'
import { CALL_WEBSOCKET_EVENTS } from '../constants/call.constants'
import type { IncomingCallPayload, CallEndedPayload } from '../types/call.types'
import toast from 'react-hot-toast'

export const useCallWebSocket = () => {
  const userId        = useSessionStore((state) => state.userId)
  const { setIncomingCall, setCallEnded, clearSession } = useCallStore()

  // Dùng ref để đọc session mới nhất trong callback mà không cần re-subscribe
  const sessionRef = useRef(useCallStore.getState().session)
  useEffect(() => {
    return useCallStore.subscribe((state) => {
      sessionRef.current = state.session
    })
  }, [])

  useEffect(() => {
    if (!userId) return

    const incomingTopic = `/user/${userId}/queue/${CALL_WEBSOCKET_EVENTS.INCOMING_CALL}`
    const endedTopic    = `/user/${userId}/queue/${CALL_WEBSOCKET_EVENTS.CALL_ENDED}`

    websocketService.subscribe(incomingTopic, (frame) => {
      console.log('[WS] incoming_call frame received:', frame.body)
      const payload: IncomingCallPayload = JSON.parse(frame.body)
      console.log('[WS] parsed payload:', payload)
      const session = sessionRef.current

      // Đang bận → thông báo và bỏ qua
      if (session?.status === 'connected' || session?.status === 'outgoing') {
        toast(`${payload.callerName} đang gọi nhưng bạn đang bận`, { icon: '📵' })
        return
      }

      setIncomingCall(payload, userId, '')
      console.log('[WS] setIncomingCall called, store:', useCallStore.getState().session)
    })

    websocketService.subscribe(endedTopic, (frame) => {
      const payload: CallEndedPayload = JSON.parse(frame.body)
      const session = sessionRef.current

      if (session?.callId === payload.callId) {
        setCallEnded()
        setTimeout(clearSession, 2000)
      }
    })

    return () => {
      websocketService.unsubscribe(incomingTopic)
      websocketService.unsubscribe(endedTopic)
    }
  // Chỉ re-subscribe khi userId thay đổi (login/logout)
  // session được đọc qua ref → không cần trong deps
  }, [userId, setIncomingCall, setCallEnded, clearSession])
}