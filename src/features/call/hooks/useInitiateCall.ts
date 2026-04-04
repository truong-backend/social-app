import { useCallback } from 'react'
import { initiateCallApi, endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useZegoClient } from './useZegoClient'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useInitiateCall = () => {
  const userId = useSessionStore((state) => state.userId) ?? ''
  const { setOutgoingCall, setCallEnded, clearSession } = useCallStore()
  const { makeCall, hangUp } = useZegoClient()

  const startCall = useCallback(async (
    targetUserId: string,
    targetName: string,
    isVideoCall: boolean,
    myName: string,
  ) => {
    try {
      // 1. Gọi BE — nhận callId tạm, push incoming_call WS đến callee
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

      // 3. Join ZegoCloud room & publish stream
      await makeCall(userId, targetUserId, isVideoCall)
    } catch (error) {
      console.error('[startCall] error:', error)
      toast.error(extractErrorMessage(error))

      // FIX Bug 4: Khi makeCall fail, notify BE để hủy call
      // Tránh callee vẫn thấy incoming modal treo mãi
      const session = useCallStore.getState().session
      if (session?.callId) {
        endCallApi(session.callId, session.receiverId).catch(() => {})
      }

      await hangUp()
      setCallEnded()
      setTimeout(clearSession, 2000)
    }
  }, [userId, setOutgoingCall, setCallEnded, clearSession, makeCall, hangUp])

  const endCall = useCallback(async () => {
    const session = useCallStore.getState().session
    await hangUp()
    if (session?.callId) {
      const targetUserId = session.callerId === userId
        ? session.receiverId
        : session.callerId
      await endCallApi(session.callId, targetUserId).catch(() => {})
    }
  }, [hangUp, userId])

  return { startCall, endCall }
}