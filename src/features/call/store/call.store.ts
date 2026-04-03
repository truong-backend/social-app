import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { CallSession, CallStatus, IncomingCallPayload } from '../types/call.types'

interface CallState {
  session:       CallSession | null
  stringeeToken: string | null
  isMicMuted:    boolean
  isCameraOff:   boolean

  // Setters
  setStringeeToken:  (token: string) => void
  setIncomingCall:   (payload: IncomingCallPayload, currentUserId: string, currentUserName: string) => void
  setOutgoingCall:   (params: {
    callId:       string
    callerId:     string
    callerName:   string
    receiverId:   string
    receiverName: string
    isVideoCall:  boolean
  }) => void
  setCallStatus:     (status: CallStatus) => void
  setCallStarted:    () => void
  setCallEnded:      () => void
  clearSession:      () => void
  toggleMic:         () => void
  toggleCamera:      () => void
}

export const useCallStore = create<CallState>()(
  immer((set) => ({
    session:       null,
    stringeeToken: null,
    isMicMuted:    false,
    isCameraOff:   false,

    setStringeeToken: (token) =>
      set((state) => { state.stringeeToken = token }),

    setIncomingCall: (payload, currentUserId, currentUserName) =>
      set((state) => {
        state.session = {
          callId:       payload.callId,
          callerId:     payload.callerId,
          callerName:   payload.callerName,
          receiverId:   currentUserId,
          receiverName: currentUserName,
          isVideoCall:  payload.isVideoCall,
          status:       'incoming',
          startedAt:    null,
          endedAt:      null,
        }
      }),

    setOutgoingCall: (params) =>
      set((state) => {
        state.session = { ...params, status: 'outgoing', startedAt: null, endedAt: null }
      }),

    setCallStatus: (status) =>
      set((state) => {
        if (state.session) state.session.status = status
      }),

    setCallStarted: () =>
      set((state) => {
        if (state.session) {
          state.session.status    = 'connected'
          state.session.startedAt = new Date().toISOString()
        }
      }),

    setCallEnded: () =>
      set((state) => {
        if (state.session) {
          state.session.status  = 'ended'
          state.session.endedAt = new Date().toISOString()
        }
      }),

    clearSession: () =>
      set((state) => {
        state.session    = null
        state.isMicMuted = false
        state.isCameraOff = false
      }),

    toggleMic: () =>
      set((state) => { state.isMicMuted = !state.isMicMuted }),

    toggleCamera: () =>
      set((state) => { state.isCameraOff = !state.isCameraOff }),
  })),
)