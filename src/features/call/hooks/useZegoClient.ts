// src/features/call/hooks/useZegoClient.ts
import { useCallback, useRef } from 'react'
import { getZegoTokenApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { ZegoSingleton, attachLocalStream, attachRemoteStream } from '../services/zego.singleton'
import { useSessionStore } from '@stores/session.store'

// Prevent double-init
let _initCalled = false

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const getSDK = (): any => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const SDK = (window as any).ZegoExpressEngine
  if (!SDK) throw new Error('[ZegoCloud] SDK chưa được load — kiểm tra index.html')
  return SDK
}

/**
 * roomID convention: "call-{smallerUserId}-{largerUserId}" để cả 2 user join cùng room.
 * streamID: "{userId}-stream"
 */
const buildRoomId = (userA: string, userB: string): string => {
  const [a, b] = [userA, userB].sort()
  return `call-${a}-${b}`.slice(0, 128)
}

export const useZegoClient = () => {
  const {
    setZegoToken, setCallStarted, setCallEnded, clearSession, setIncomingCall
  } = useCallStore()

  const localStreamIdRef = useRef<string>('')

  // ── Init engine singleton (1 lần cho toàn app) ──────────────
  const initEngine = useCallback(async () => {
    if (_initCalled) return
    if (ZegoSingleton.getEngine()) return
    _initCalled = true

    try {
      const { token, appId } = await getZegoTokenApi()
      setZegoToken(token)

      const ZegoExpressEngine = getSDK()
      const engine = new ZegoExpressEngine(appId, 'wss://webliveroom-api.zego.im/ws')
      ZegoSingleton.setEngine(engine)

      // Room state callback
      engine.on('roomStateChanged', (...args: unknown[]) => {
        const [, , errorCode] = args as [string, string, number]
        if (errorCode !== 0) {
          console.error('[ZegoCloud] roomStateChanged error:', errorCode)
        }
      })

      // Remote stream added
      engine.on('roomStreamUpdate', async (...args: unknown[]) => {
        const [, updateType, streamList] = args as [string, string, Array<{ streamID: string }>]
        if (updateType === 'ADD' && streamList?.length) {
          for (const s of streamList) {
            try {
              const remoteStream = await engine.startPlayingStream(s.streamID)
              attachRemoteStream(remoteStream)
            } catch (err) {
              console.error('[ZegoCloud] startPlayingStream failed:', err)
            }
          }
        }
        if (updateType === 'DELETE' && streamList?.length) {
          for (const s of streamList) {
            engine.stopPlayingStream(s.streamID)
          }
        }
      })

      // Remote user left → call ended
      engine.on('roomUserUpdate', (...args: unknown[]) => {
        const [, updateType] = args as [string, string]
        if (updateType === 'LEAVE') {
          setCallEnded()
          ZegoSingleton.setLocalStream(null)
          ZegoSingleton.setRemoteStream(null)
          setTimeout(clearSession, 2000)
        }
      })

      console.log('[ZegoCloud] engine initialized')
    } catch (err) {
      console.error('[ZegoCloud] initEngine failed:', err)
      _initCalled = false
    }
  }, [setZegoToken, setCallEnded, clearSession, setIncomingCall])

  // ── Join room & publish stream (caller) ─────────────────────
  const makeCall = useCallback(async (
    fromUserId: string,
    toUserId: string,
    isVideoCall: boolean,
  ) => {
    const engine = ZegoSingleton.getEngine()
    if (!engine) throw new Error('[ZegoCloud] Engine chưa init')

    const { token } = await getZegoTokenApi()
    const roomID = buildRoomId(fromUserId, toUserId)
    const streamID = `${fromUserId}-stream`
    localStreamIdRef.current = streamID

    await engine.loginRoom(roomID, token, { userID: fromUserId, userName: fromUserId })

    const localStream = await engine.createStream({
      camera: { audio: true, video: isVideoCall }
    })
    attachLocalStream(localStream)
    engine.startPublishingStream(streamID, localStream)
    setCallStarted()

    console.log('[ZegoCloud] makeCall — room:', roomID, 'stream:', streamID)
  }, [setCallStarted])

  // ── Join room & publish stream (receiver) ───────────────────
  const answerCall = useCallback(async (
    myUserId: string,
    callerUserId: string,
    isVideoCall: boolean,
  ) => {
    const engine = ZegoSingleton.getEngine()
    if (!engine) { console.warn('[ZegoCloud] answerCall — no engine'); return }

    const { token } = await getZegoTokenApi()
    const roomID = buildRoomId(myUserId, callerUserId)
    const streamID = `${myUserId}-stream`
    localStreamIdRef.current = streamID

    await engine.loginRoom(roomID, token, { userID: myUserId, userName: myUserId })

    const localStream = await engine.createStream({
      camera: { audio: true, video: isVideoCall }
    })
    attachLocalStream(localStream)
    engine.startPublishingStream(streamID, localStream)
    setCallStarted()

    console.log('[ZegoCloud] answerCall — room:', roomID, 'stream:', streamID)
  }, [setCallStarted])

  // ── Hang up / leave room ─────────────────────────────────────
  const hangUp = useCallback(async () => {
    const engine = ZegoSingleton.getEngine()
    if (!engine) return

    if (localStreamIdRef.current) {
      engine.stopPublishingStream(localStreamIdRef.current)
    }
    const localStream = ZegoSingleton.getLocalStream()
    if (localStream) engine.destroyStream(localStream)
    ZegoSingleton.setLocalStream(null)
    ZegoSingleton.setRemoteStream(null)

    await engine.logoutRoom().catch(() => {})
    console.log('[ZegoCloud] hangUp — room left')
  }, [])

  // ── Mute / Video toggle ──────────────────────────────────────
  const setMuted = useCallback((muted: boolean) => {
    const engine = ZegoSingleton.getEngine()
    if (!engine || !localStreamIdRef.current) return
    engine.mutePublishStreamAudio(localStreamIdRef.current, muted)
  }, [])

  const setVideoEnabled = useCallback((enabled: boolean) => {
    const engine = ZegoSingleton.getEngine()
    if (!engine || !localStreamIdRef.current) return
    engine.mutePublishStreamVideo(localStreamIdRef.current, !enabled)
  }, [])

  const disconnectClient = useCallback(async () => {
    await hangUp()
    ZegoSingleton.setEngine(null)
    _initCalled = false
  }, [hangUp])

  return { initEngine, disconnectClient, makeCall, answerCall, hangUp, setMuted, setVideoEnabled }
}
