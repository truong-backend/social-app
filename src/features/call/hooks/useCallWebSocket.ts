import { useEffect, useRef } from 'react'
import { websocketService } from '@services/websocket.service'
import { useCallStore } from '../store/call.store'
import { useSessionStore } from '@stores/session.store'
import { CALL_WEBSOCKET_EVENTS } from '../constants/call.constants'
import type { IncomingCallPayload, CallEndedPayload } from '../types/call.types'
import toast from 'react-hot-toast'

export const useCallWebSocket = () => {
  const userId = useSessionStore((state) => state.userId)
  const { setIncomingCall, setCallEnded, clearSession } = useCallStore()

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

      // FIX: thêm 'incoming' vào busy guard
      // tránh trường hợp đang có modal incoming mà người khác gọi override session
      if (
        session?.status === 'connected' ||
        session?.status === 'outgoing'  ||
        session?.status === 'incoming'
      ) {
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
        // FIX: thông báo rõ lý do tắt cho bên gọi
        if (session.status === 'outgoing') {
          toast('Không có ai bắt máy', { icon: '📵' })
        }
        setCallEnded()
        setTimeout(clearSession, 2000)
      }
    })

    return () => {
      websocketService.unsubscribe(incomingTopic)
      websocketService.unsubscribe(endedTopic)
    }
  }, [userId, setIncomingCall, setCallEnded, clearSession])
}