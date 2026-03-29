import { useEffect, useRef, useCallback } from 'react'
import { useCallStore } from '../store/call.store'
import { getStringeeTokenApi } from '../api/call.api'

// Stringee SDK được load qua <script> trong index.html
// Tham chiếu qua window để tránh TypeScript lỗi
declare global {
  interface Window {
    StringeeClient: new () => StringeeClientInstance
    StringeeCall:   new (client: StringeeClientInstance, from: string, to: string, isVideoCall: boolean) => StringeeCallInstance
    StringeeCall2:  new (client: StringeeClientInstance, from: string, to: string[], isVideoCall: boolean) => StringeeCallInstance
  }
}

interface StringeeClientInstance {
  connect:            (token: string) => void
  disconnect:         () => void
  on:                 (event: string, handler: (...args: unknown[]) => void) => void
}

interface StringeeCallInstance {
  makeCall:           (callback: (result: { r: number; message: string; callId: string }) => void) => void
  answer:             (callback: (result: { r: number }) => void) => void
  hangup:             (callback: (result: { r: number }) => void) => void
  mute:               (muted: boolean) => void
  enableVideo:        (enabled: boolean) => void
  on:                 (event: string, handler: (...args: unknown[]) => void) => void
  localStream:        MediaStream | null
  remoteStream:       MediaStream | null
}

export const useStringeeClient = () => {
  const clientRef = useRef<StringeeClientInstance | null>(null)
  const callRef   = useRef<StringeeCallInstance | null>(null)

  const { setStringeeToken, setCallStarted, setCallEnded, clearSession } = useCallStore()

  // ── Initialise client ────────────────────────────────────────
  const initClient = useCallback(async () => {
    if (!window.StringeeClient) {
      console.error('[Stringee] SDK not loaded. Check <script> tag in index.html')
      return
    }

    const token = await getStringeeTokenApi()
    setStringeeToken(token)

    const client = new window.StringeeClient()
    clientRef.current = client

    client.on('connect', () => {
      console.log('[Stringee] Client connected')
    })

    client.on('authen', (result: unknown) => {
      const res = result as { r: number; message: string }
      if (res.r !== 0) {
        console.error('[Stringee] Auth failed:', res.message)
      }
    })

    client.on('disconnect', () => {
      console.log('[Stringee] Client disconnected')
    })

    client.connect(token)
  }, [setStringeeToken])

  // ── Make outgoing call ────────────────────────────────────────
  const makeCall = useCallback(
    (
      fromUserId: string,
      toUserId:   string,
      isVideoCall: boolean,
      localVideoRef:  React.RefObject<HTMLVideoElement>,
      remoteVideoRef: React.RefObject<HTMLVideoElement>,
    ): Promise<string> => {
      return new Promise((resolve, reject) => {
        if (!clientRef.current) {
          reject(new Error('Stringee client not initialised'))
          return
        }

        const call = new window.StringeeCall(
          clientRef.current,
          fromUserId,
          toUserId,
          isVideoCall,
        )
        callRef.current = call

        bindCallEvents(call, localVideoRef, remoteVideoRef)

        call.makeCall((result) => {
          if (result.r !== 0) {
            reject(new Error(result.message))
          } else {
            resolve(result.callId)
          }
        })
      })
    },
    [],
  )

  // ── Answer incoming call ──────────────────────────────────────
  const answerCall = useCallback(
    (
      localVideoRef:  React.RefObject<HTMLVideoElement>,
      remoteVideoRef: React.RefObject<HTMLVideoElement>,
    ) => {
      const call = callRef.current
      if (!call) return

      bindCallEvents(call, localVideoRef, remoteVideoRef)

      call.answer((result) => {
        if (result.r === 0) {
          setCallStarted()
        }
      })
    },
    [setCallStarted],
  )

  // ── Hang up ───────────────────────────────────────────────────
  const hangUp = useCallback(() => {
    callRef.current?.hangup(() => {
      setCallEnded()
      setTimeout(clearSession, 2000)
    })
  }, [setCallEnded, clearSession])

  // ── Mute / Camera ─────────────────────────────────────────────
  const setMuted = useCallback((muted: boolean) => {
    callRef.current?.mute(muted)
  }, [])

  const setVideoEnabled = useCallback((enabled: boolean) => {
    callRef.current?.enableVideo(enabled)
  }, [])

  // ── Attach inbound call (from WebSocket) ──────────────────────
  const attachIncomingCall = useCallback((stringeeCallObject: StringeeCallInstance) => {
    callRef.current = stringeeCallObject
  }, [])

  // ── Disconnect ────────────────────────────────────────────────
  const disconnectClient = useCallback(() => {
    clientRef.current?.disconnect()
    clientRef.current = null
    callRef.current   = null
  }, [])

  // ── Bind call events ─────────────────────────────────────────
  const bindCallEvents = (
    call:           StringeeCallInstance,
    localVideoRef:  React.RefObject<HTMLVideoElement>,
    remoteVideoRef: React.RefObject<HTMLVideoElement>,
  ) => {
    call.on('addlocalstream', (stream: unknown) => {
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream as MediaStream
      }
    })

    call.on('addremotestream', (stream: unknown) => {
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = stream as MediaStream
      }
      setCallStarted()
    })

    call.on('signalingstate', (state: unknown) => {
      const s = state as { code: number; reason: string }
      // code 6 = ended / busy / rejected
      if (s.code === 6 || s.code === 5 || s.code === 3) {
        setCallEnded()
        setTimeout(clearSession, 2000)
      }
    })

    call.on('mediastate', (state: unknown) => {
      console.log('[Stringee] Media state:', state)
    })
  }

  return {
    initClient,
    makeCall,
    answerCall,
    hangUp,
    setMuted,
    setVideoEnabled,
    attachIncomingCall,
    disconnectClient,
    clientRef,
    callRef,
  }
}