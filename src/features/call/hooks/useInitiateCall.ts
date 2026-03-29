import { useCallback, useRef } from 'react'
import { initiateCallApi, endCallApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from './useStringeeClient'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

export const useInitiateCall = () => {
  const userId         = useSessionStore((state) => state.userId) ?? ''
  const { setOutgoingCall, setCallEnded, clearSession } = useCallStore()
  const { makeCall, hangUp } = useStringeeClient()

  const localVideoRef  = useRef<HTMLVideoElement>(null)
  const remoteVideoRef = useRef<HTMLVideoElement>(null)

  const startCall = useCallback(
    async (
      targetUserId:  string,
      targetName:    string,
      isVideoCall:   boolean,
      myName:        string,
    ) => {
      try {
        // 1. Gọi BE — tạo Call entity + push WebSocket đến receiver
        const { callId, messageId, chatId } = await initiateCallApi(targetUserId, isVideoCall)

        // 2. Cập nhật store
        setOutgoingCall({
          callId,
          messageId,
          chatId,
          callerId:     userId,
          callerName:   myName,
          receiverId:   targetUserId,
          receiverName: targetName,
          isVideoCall,
        })

        // 3. Kết nối Stringee
        await makeCall(userId, targetUserId, isVideoCall, localVideoRef, remoteVideoRef)
      } catch (error) {
        toast.error(extractErrorMessage(error))
        setCallEnded()
        setTimeout(clearSession, 2000)
      }
    },
    [userId, setOutgoingCall, setCallEnded, clearSession, makeCall],
  )

  const endCall = useCallback(async () => {
    const callId = useCallStore.getState().session?.callId
    hangUp()
    if (callId) {
      await endCallApi(callId).catch(() => {
        // Không block UI nếu API fail
      })
    }
  }, [hangUp])

  return { startCall, endCall, localVideoRef, remoteVideoRef }
}