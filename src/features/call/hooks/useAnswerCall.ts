import { useCallback } from 'react'
import { endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from './useStringeeClient'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useAnswerCall = () => {
  const { setCallEnded, clearSession } = useCallStore()
  const { answerCall, hangUp } = useStringeeClient()

  const acceptCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    try {
      // Không gọi BE để "answer" — không có endpoint đó.
      // Stringee SDK tự xử lý: signalingstate code=3 → setCallStarted()
      answerCall()
    } catch (error) {
      toast.error(extractErrorMessage(error))
      setCallEnded()
      setTimeout(clearSession, 2000)
    }
  }, [setCallEnded, clearSession, answerCall])

  const rejectCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    hangUp()
    // Push call_ended đến caller qua BE
    // Backend RejectCallUseCase: POST /api/calls/{callId}/reject?callerUserId=xxx
    await endCallApi(session.callId, session.callerId).catch(() => {})
  }, [hangUp])

  return { acceptCall, rejectCall }
}