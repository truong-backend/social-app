import { useCallback } from 'react'
import { getStringeeTokenApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { StringeeSingleton, attachLocalStream, attachRemoteStream } from '../services/stringee.singleton'
import type { StringeeCallInstance } from '../types/stringee.types'
import { useSessionStore } from '@stores/session.store'

// Module-level flag — tồn tại suốt lifetime app, không bị reset khi re-render
let _initCalled = false

const getSDK = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const SDK = (window as any).StringeeClient
  if (!SDK) throw new Error('[Stringee] SDK chưa được load — kiểm tra index.html')
  return SDK
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const getCallSDK = () => (window as any).StringeeCall

export const useStringeeClient = () => {
  const { setStringeeToken, setCallStarted, setCallEnded, clearSession, setIncomingCall } = useCallStore()

  // ── Bind events lên call object ──────────────────────────────
  const _bindCallEvents = useCallback((call: StringeeCallInstance) => {
    call.on('addlocalstream', (stream: unknown) => {
      console.log('[Stringee] local stream')
      attachLocalStream(stream as MediaStream)
    })

    call.on('addremotestream', (stream: unknown) => {
      console.log('[Stringee] remote stream')
      attachRemoteStream(stream as MediaStream)
    })

    call.on('signalingstate', (...args: unknown[]) => {
      const state = args[0] as { code: number; reason: string }
      console.log('[Stringee] signalingstate:', state.code, state.reason)
      if (state.code === 3) {
        // answered
        setCallStarted()
      } else if (state.code === 4 || state.code === 5 || state.code === 6) {
        // ended / busy / not found
        setCallEnded()
        StringeeSingleton.setCall(null)
        setTimeout(clearSession, 2000)
      }
    })

    call.on('mediastate', (...args: unknown[]) => {
      const state = args[0] as { code: number }
      console.log('[Stringee] mediastate:', state.code)
    })

    call.on('otherdevice', (data: unknown) => {
      console.log('[Stringee] otherdevice:', data)
    })
  }, [setCallStarted, setCallEnded, clearSession])

  // ── Init client — chỉ chạy 1 lần duy nhất ───────────────────
  const initClient = useCallback(async () => {
    if (_initCalled) return
    if (StringeeSingleton.getClient()) return
    _initCalled = true

    try {
      const token = await getStringeeTokenApi()
      setStringeeToken(token)

      const StringeeClient = getSDK()
      const client = new StringeeClient()
      StringeeSingleton.setClient(client)

      client.on('connect', () => console.log('[Stringee] connected'))

      client.on('authen', (...args: unknown[]) => {
        const res = args[0] as { r: number; userId: string }
        if (res.r === 0) console.log('[Stringee] Auth OK:', res.userId)
        else console.error('[Stringee] Auth failed:', res)
      })

      client.on('disconnect', () => {
        console.log('[Stringee] disconnected')
        _initCalled = false
        StringeeSingleton.setClient(null)
      })

      /**
       * incomingcall — Stringee SDK push peer-to-peer, độc lập với WS.
       *
       * Luồng đúng: WS incoming_call → setIncomingCall() → UI hiện modal
       *             Stringee incomingcall → lưu call object vào Singleton (để answer)
       *
       * Fallback: nếu WS chưa đến (chậm / lỗi) mà Stringee đã fire,
       * tự gọi setIncomingCall() dùng data từ call object của Stringee.
       * Tránh trường hợp người nghe thấy camera/mic hoạt động nhưng không có modal.
       */
      client.on('incomingcall', (...args: unknown[]) => {
        const incomingCall = args[0] as StringeeCallInstance
        console.log('[Stringee] incomingcall — lưu call object vào Singleton')
        StringeeSingleton.setCall(incomingCall)
        _bindCallEvents(incomingCall)

        // Fallback: nếu store chưa có session (WS incoming_call chưa đến)
        // → tự trigger UI bằng data từ Stringee call object
        const currentSession = useCallStore.getState().session
        if (!currentSession || currentSession.status === 'idle') {
          const currentUserId = useSessionStore.getState().userId ?? ''
          console.log('[Stringee] incomingcall fallback → setIncomingCall from Stringee data')
          setIncomingCall(
            {
              callId:      incomingCall.callId   ?? 'stringee-' + Date.now(),
              callerId:    incomingCall.fromNumber ?? '',
              callerName:  incomingCall.fromNumber ?? 'Unknown',
              isVideoCall: incomingCall.isVideoCall,
            },
            currentUserId,
            '',
          )
        }
      })

      client.connect(token)
    } catch (err) {
      console.error('[Stringee] initClient failed:', err)
      _initCalled = false
    }
  }, [setStringeeToken, _bindCallEvents, setIncomingCall])

  // ── Disconnect ───────────────────────────────────────────────
  const disconnectClient = useCallback(() => {
    StringeeSingleton.getClient()?.disconnect()
    StringeeSingleton.setClient(null)
    StringeeSingleton.setCall(null)
    _initCalled = false
  }, [])

  // ── Make call (caller) ───────────────────────────────────────
  const makeCall = useCallback(async (
    fromUserId: string,
    toUserId: string,
    isVideoCall: boolean,
  ) => {
    const client = StringeeSingleton.getClient()
    if (!client) throw new Error('[Stringee] Client chưa init')

    const StringeeCall = getCallSDK()
    if (!StringeeCall) throw new Error('[Stringee] StringeeCall SDK chưa load')

    const call: StringeeCallInstance = new StringeeCall(client, fromUserId, toUserId, isVideoCall)
    StringeeSingleton.setCall(call)
    _bindCallEvents(call)

    return new Promise<void>((resolve, reject) => {
      call.makeCall((res) => {
        if (res.r === 0) {
          console.log('[Stringee] makeCall OK:', res.callId)
          resolve()
        } else {
          reject(new Error(res.message))
        }
      })
    })
  }, [_bindCallEvents])

  // ── Answer call (receiver) ───────────────────────────────────
  const answerCall = useCallback(() => {
    const call = StringeeSingleton.getCall()
    if (!call) { console.warn('[Stringee] answerCall — không có call object'); return }
    call.answer((res) => console.log('[Stringee] answer:', res.r))
  }, [])

  // ── Hang up ──────────────────────────────────────────────────
  const hangUp = useCallback(() => {
    const call = StringeeSingleton.getCall()
    if (!call) return
    call.hangup((res) => console.log('[Stringee] hangup:', res.r))
    StringeeSingleton.setCall(null)
  }, [])

  // ── Mute / Video ─────────────────────────────────────────────
  const setMuted        = useCallback((muted: boolean)   => { StringeeSingleton.getCall()?.mute(muted) }, [])
  const setVideoEnabled = useCallback((enabled: boolean) => { StringeeSingleton.getCall()?.enableVideo(enabled) }, [])

  return { initClient, disconnectClient, makeCall, answerCall, hangUp, setMuted, setVideoEnabled }
}
