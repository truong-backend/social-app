import { useCallback, useRef } from 'react'
import { answerCallApi, endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from './useStringeeClient'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useAnswerCall = () => {
  const { setCallStatus, setCallEnded, clearSession } = useCallStore()
  const { answerCall, hangUp } = useStringeeClient()

  const localVideoRef  = useRef<HTMLVideoElement>(null)
  const remoteVideoRef = useRef<HTMLVideoElement>(null)

  const acceptCall = useCallback(async () => {
    const session = useCallStore.getState().session
    if (!session) return

    try {
      setCallStatus('connected')
      await answerCallApi(session.callId)
      answerCall(localVideoRef, remoteVideoRef)
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
    setCallEnded()
    setTimeout(clearSession, 1500)
  }, [hangUp, setCallEnded, clearSession])

  return { acceptCall, rejectCall, localVideoRef, remoteVideoRef }
}