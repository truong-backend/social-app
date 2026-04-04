// src/features/call/hooks/useZegoClient.ts
import { useCallback, useRef } from 'react'
import { ZegoExpressEngine } from 'zego-express-engine-webrtc'
import { getZegoTokenApi } from '../api/call.api'
import { useCallStore } from '../store/call.store'
import { ZegoSingleton, attachLocalStream, attachRemoteStream } from '../services/zego.singleton'

// Prevent double-init
let _initCalled = false

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
    setZegoToken, setCallStarted, setCallEnded, clearSession
  } = useCallStore()

  const localStreamIdRef = useRef<string>('')

  // ── Tạo / lấy engine instance ────────────────────────────────
  const getOrCreateEngine = useCallback(async (): Promise<ZegoExpressEngine> => {
    const existing = ZegoSingleton.getEngine() as ZegoExpressEngine | null
    if (existing) return existing

    const { appId } = await getZegoTokenApi()
    if (!appId) throw new Error('[ZegoCloud] APP_ID chưa được cấu hình (VITE_ZEGOCLOUD_APP_ID)')

    const engine = new ZegoExpressEngine(appId, 'wss://webliveroom-api.zego.im/ws')
    ZegoSingleton.setEngine(engine as unknown as import('../types/zego.types').ZegoEngineInstance)

    // Remote stream added
    engine.on('roomStreamUpdate', async (roomID: string, updateType: string, streamList: Array<{ streamID: string }>) => {
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

    // FIX Bug 2: Chỉ kết thúc call khi session đang 'connected'
    // Tránh LEAVE event fire sớm khi chỉ có 1 người trong phòng (phantom leave)
    engine.on('roomUserUpdate', (_roomID: string, updateType: string) => {
      if (updateType === 'LEAVE') {
        const session = useCallStore.getState().session
        if (session?.status !== 'connected') {
          console.log('[ZegoCloud] roomUserUpdate LEAVE ignored — session not connected:', session?.status)
          return
        }
        console.log('[ZegoCloud] roomUserUpdate LEAVE — ending call')
        setCallEnded()
        ZegoSingleton.setLocalStream(null)
        ZegoSingleton.setRemoteStream(null)
        setTimeout(clearSession, 2000)
      }
    })

    console.log('[ZegoCloud] engine created, appId:', appId)
    return engine
  }, [setCallEnded, clearSession])

  // ── Init engine singleton (1 lần cho toàn app) ──────────────
  const initEngine = useCallback(async () => {
    if (_initCalled) return
    _initCalled = true

    try {
      const engine = await getOrCreateEngine()
      const { token } = await getZegoTokenApi()
      setZegoToken(token)
      console.log('[ZegoCloud] engine ready')
      void engine // keep reference
    } catch (err) {
      console.error('[ZegoCloud] initEngine failed:', err)
      _initCalled = false
    }
  }, [getOrCreateEngine, setZegoToken])

  // ── Join room & publish stream (caller) ─────────────────────
  const makeCall = useCallback(async (
    fromUserId: string,
    toUserId: string,
    isVideoCall: boolean,
  ) => {
    const engine = await getOrCreateEngine()

    const { token } = await getZegoTokenApi()
    const roomID = buildRoomId(fromUserId, toUserId)
    const streamID = `${fromUserId}-stream`
    localStreamIdRef.current = streamID

    // FIX Bug 1: loginRoom trả về Promise<boolean> — false = thất bại, KHÔNG throw
    // Nếu không check, createStream() chạy khi chưa vào phòng → crash
    const loginOk = await engine.loginRoom(roomID, token, { userID: fromUserId, userName: fromUserId })
    if (!loginOk) {
      throw new Error('[ZegoCloud] loginRoom thất bại — kiểm tra token hoặc appId')
    }

    const localStream = await engine.createStream({
      camera: { audio: true, video: isVideoCall }
    })
    attachLocalStream(localStream)
    engine.startPublishingStream(streamID, localStream)
    setCallStarted()

    console.log('[ZegoCloud] makeCall — room:', roomID, 'stream:', streamID)
  }, [getOrCreateEngine, setCallStarted])

  // ── Join room & publish stream (receiver) ───────────────────
  const answerCall = useCallback(async (
    myUserId: string,
    callerUserId: string,
    isVideoCall: boolean,
  ) => {
    const engine = await getOrCreateEngine()

    const { token } = await getZegoTokenApi()
    const roomID = buildRoomId(myUserId, callerUserId)
    const streamID = `${myUserId}-stream`
    localStreamIdRef.current = streamID

    // FIX Bug 1: tương tự makeCall
    const loginOk = await engine.loginRoom(roomID, token, { userID: myUserId, userName: myUserId })
    if (!loginOk) {
      throw new Error('[ZegoCloud] loginRoom thất bại — kiểm tra token hoặc appId')
    }

    const localStream = await engine.createStream({
      camera: { audio: true, video: isVideoCall }
    })
    attachLocalStream(localStream)
    engine.startPublishingStream(streamID, localStream)
    setCallStarted()

    console.log('[ZegoCloud] answerCall — room:', roomID, 'stream:', streamID)
  }, [getOrCreateEngine, setCallStarted])

  // ── Hang up / leave room ─────────────────────────────────────
  const hangUp = useCallback(async () => {
    const engine = ZegoSingleton.getEngine() as ZegoExpressEngine | null
    if (!engine) return

    if (localStreamIdRef.current) {
      engine.stopPublishingStream(localStreamIdRef.current)
    }
    const localStream = ZegoSingleton.getLocalStream()
    if (localStream) engine.destroyStream(localStream)
    ZegoSingleton.setLocalStream(null)
    ZegoSingleton.setRemoteStream(null)

    try { engine.logoutRoom() } catch { /* ignore */ }
    console.log('[ZegoCloud] hangUp — room left')
  }, [])

  // ── Mute / Video toggle ──────────────────────────────────────
  const setMuted = useCallback((muted: boolean) => {
    const engine = ZegoSingleton.getEngine() as ZegoExpressEngine | null
    if (!engine) return
    const localStream = ZegoSingleton.getLocalStream()
    if (!localStream) return
    engine.mutePublishStreamAudio(localStream, muted)
  }, [])

  const setVideoEnabled = useCallback((enabled: boolean) => {
    const engine = ZegoSingleton.getEngine() as ZegoExpressEngine | null
    if (!engine) return
    const localStream = ZegoSingleton.getLocalStream()
    if (!localStream) return
    engine.mutePublishStreamVideo(localStream, !enabled)
  }, [])

  const disconnectClient = useCallback(async () => {
    await hangUp()
    ZegoSingleton.setEngine(null)
    _initCalled = false
  }, [hangUp])

  return { initEngine, disconnectClient, makeCall, answerCall, hangUp, setMuted, setVideoEnabled }
}