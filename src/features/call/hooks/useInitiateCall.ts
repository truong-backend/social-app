import { useCallback } from 'react'
import { initiateCallApi, endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from './useStringeeClient'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useInitiateCall = () => {
  const userId = useSessionStore((state) => state.userId) ?? ''
  const { setOutgoingCall, setCallEnded, clearSession } = useCallStore()
  const { makeCall, hangUp } = useStringeeClient()

  const startCall = useCallback(async (
    targetUserId: string,
    targetName: string,
    isVideoCall: boolean,
    myName: string,
  ) => {
    try {
      // 1. Gọi BE — nhận callId tạm
      const { callId } = await initiateCallApi(targetUserId, isVideoCall)

      // 2. Update store TRƯỚC khi makeCall → CallScreen hiện ngay
      setOutgoingCall({
        callId,
        callerId: userId,
        callerName: myName,
        receiverId: targetUserId,
        receiverName: targetName,
        isVideoCall,
      })
      console.log('[startCall] store updated, status should be outgoing')
      console.log('[startCall] store state:', useCallStore.getState().session?.status)

      // 3. Kết nối Stringee
      await makeCall(userId, targetUserId, isVideoCall)
    } catch (error) {
      console.error('[startCall] error:', error)
      toast.error(extractErrorMessage(error))
      setCallEnded()
      setTimeout(clearSession, 2000)
    }
  }, [userId, setOutgoingCall, setCallEnded, clearSession, makeCall])

  const endCall = useCallback(async () => {
    const session = useCallStore.getState().session
    hangUp()
    if (session?.callId) {
      const targetUserId = session.callerId === userId
        ? session.receiverId
        : session.callerId
      await endCallApi(session.callId, targetUserId).catch(() => {})
    }
  }, [hangUp, userId])

  return { startCall, endCall }
}