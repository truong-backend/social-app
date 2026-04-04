import { useCallback } from 'react'
import { endCallApi, rejectCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useZegoClient } from './useZegoClient'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useAnswerCall = () => {
  const { setCallEnded, clearSession } = useCallStore()
  const { answerCall: zegoAnswer, hangUp } = useZegoClient()
  const userId = useSessionStore((state) => state.userId) ?? ''

  const acceptCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    try {
      // Join ZegoCloud room với roomID tương ứng caller
      await zegoAnswer(userId, session.callerId, session.isVideoCall)
    } catch (error) {
      toast.error(extractErrorMessage(error))

      // Notify BE để hủy call khi join thất bại
      endCallApi(session.callId, session.callerId).catch(() => {})

      await hangUp()
      setCallEnded()
      setTimeout(clearSession, 2000)
    }
  }, [userId, setCallEnded, clearSession, zegoAnswer, hangUp])

  const rejectCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    await hangUp()

    // FIX Bug 3: Phải gọi đúng endpoint /reject?callerUserId=xxx
    // KHÔNG dùng endCallApi (POST /end) vì endpoint khác nhau
    // BE RejectCallUseCase: POST /api/calls/{callId}/reject?callerUserId=xxx
    await rejectCallApi(session.callId, session.callerId).catch(() => {})
  }, [hangUp])

  return { acceptCall, rejectCall }
}