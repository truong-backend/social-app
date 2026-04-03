import { useCallback } from 'react'
import { answerCallApi, endCallApi } from '../api/call.api'
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
      // KHÔNG set 'connected' thủ công ở đây
      // signalingstate code=3 sẽ tự gọi setCallStarted() → render CallScreen đúng lúc
      await answerCallApi(session.callId)
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
    await endCallApi(session.callId).catch(() => {})
  }, [hangUp])

  return { acceptCall, rejectCall }
}