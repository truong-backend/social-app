import { useCallback } from 'react'
import { getStringeeTokenApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { StringeeSingleton, attachLocalStream, attachRemoteStream } from '../services/stringee.singleton'
import type { StringeeCallInstance } from '../types/stringee.types'

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
  const { setStringeeToken, setCallStarted, setCallEnded, clearSession } = useCallStore()

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
    // Double-check: cả module flag lẫn singleton
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
        // Reset flag để có thể reconnect nếu cần
        _initCalled = false
        StringeeSingleton.setClient(null)
      })

      // Lưu call object khi có cuộc gọi đến qua Stringee SDK
      // UI được điều khiển bởi WebSocket (useCallWebSocket)
      client.on('incomingcall', (...args: unknown[]) => {
        const incomingCall = args[0] as StringeeCallInstance
        console.log('[Stringee] incomingcall — lưu call object vào Singleton')
        StringeeSingleton.setCall(incomingCall)
        _bindCallEvents(incomingCall)
      })

      client.connect(token)
    } catch (err) {
      console.error('[Stringee] initClient failed:', err)
      _initCalled = false
    }
  }, [setStringeeToken, _bindCallEvents])

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