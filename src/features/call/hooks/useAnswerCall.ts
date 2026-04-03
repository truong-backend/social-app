import { useCallback } from 'react'
import { answerCallApi, endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from './useStringeeClient'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useAnswerCall = () => {
  const { setCallStatus, setCallEnded, clearSession } = useCallStore()
  // FIX: dùng answerCall và hangUp từ useStringeeClient (đã có callRef đúng)
  const { answerCall, hangUp } = useStringeeClient()

  const acceptCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    try {
      setCallStatus('connected')
      await answerCallApi(session.callId)
      // FIX: answerCall không cần truyền ref nữa, ref được quản lý trong useStringeeClient
      answerCall()
    } catch (error) {
      toast.error(extractErrorMessage(error))
      setCallEnded()
      setTimeout(clearSession, 2000)
    }
  }, [setCallStatus, setCallEnded, clearSession, answerCall])

  const rejectCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    hangUp()
    await endCallApi(session.callId).catch(() => {})
  }, [hangUp])

  return { acceptCall, rejectCall }
}