import { useEffect, useState, useCallback } from 'react'
import { useCallStore } from '../store/call.store'
import { useInitiateCall } from '../hooks/useInitiateCall'
import { useStringeeClient } from '../hooks/useStringeeClient'
import { StringeeSingleton } from '../services/stringee.singleton'

export const CallScreen = () => {
  const session     = useCallStore((state) => state.session)
  const isMicMuted  = useCallStore((state) => state.isMicMuted)
  const isCameraOff = useCallStore((state) => state.isCameraOff)
  const { toggleMic, toggleCamera } = useCallStore()

  const { endCall } = useInitiateCall()
  const { setMuted, setVideoEnabled } = useStringeeClient()

  const localVideoCallbackRef = useCallback((el: HTMLVideoElement | null) => {
    StringeeSingleton.setLocalVideoEl(el)
  }, [])

  const remoteVideoCallbackRef = useCallback((el: HTMLVideoElement | null) => {
    StringeeSingleton.setRemoteVideoEl(el)
  }, [])

  const [callDurationSeconds, setCallDurationSeconds] = useState(0)

  useEffect(() => {
    if (session?.status !== 'connected') return
    const interval = setInterval(() => {
      setCallDurationSeconds((prev) => prev + 1)
    }, 1000)
    return () => clearInterval(interval)
  }, [session?.status])

  const handleToggleMic = () => {
    setMuted(!isMicMuted)
    toggleMic()
  }

  const handleToggleCamera = () => {
    setVideoEnabled(isCameraOff)
    toggleCamera()
  }

  // Guard: chỉ render khi có session hợp lệ
  if (!session) return null
  if (!['incoming', 'outgoing', 'connected'].includes(session.status)) return null

  const formatDuration = (seconds: number) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0')
    const s = (seconds % 60).toString().padStart(2, '0')
    return `${m}:${s}`
  }

  const peerName =
    session.status === 'outgoing'
      ? session.receiverName
      : session.callerName

  // const isConnected = session.status === 'connected'
  const isIncoming  = session.status === 'incoming'

  return (
    <div
      className="call-screen"
      // Ẩn hoàn toàn khi incoming — IncomingCallModal hiển thị thay thế
      // nhưng <video> ref vẫn được bind để stream không bị miss
      style={isIncoming ? { display: 'none' } : undefined}
    >
      {session.isVideoCall ? (
        <video
          ref={remoteVideoCallbackRef}
          className="call-screen__remote-video"
          autoPlay
          playsInline
        />
      ) : (
        <div className="call-screen__voice-bg">
          <div className="call-screen__voice-avatar">
            {peerName.charAt(0).toUpperCase()}
          </div>
        </div>
      )}

      {session.isVideoCall && (
        <video
          ref={localVideoCallbackRef}
          className="call-screen__local-video"
          autoPlay
          playsInline
          muted
        />
      )}

      <div className="call-screen__top-bar">
        <p className="call-screen__peer-name">{peerName}</p>
        <p className="call-screen__status">
          {session.status === 'outgoing'
            ? 'Đang gọi...'
            : formatDuration(callDurationSeconds)}
        </p>
      </div>

      <div className="call-screen__controls">
        <button
          className={`call-screen__control-btn ${isMicMuted ? 'call-screen__control-btn--active' : ''}`}
          onClick={handleToggleMic}
          aria-label={isMicMuted ? 'Bật mic' : 'Tắt mic'}
        >
          {isMicMuted ? '🔇' : '🎤'}
        </button>

        <button
          className="call-screen__control-btn call-screen__control-btn--end"
          onClick={endCall}
          aria-label="Kết thúc cuộc gọi"
        >
          📵
        </button>

        {session.isVideoCall && (
          <button
            className={`call-screen__control-btn ${isCameraOff ? 'call-screen__control-btn--active' : ''}`}
            onClick={handleToggleCamera}
            aria-label={isCameraOff ? 'Bật camera' : 'Tắt camera'}
          >
            {isCameraOff ? '🚫' : '📷'}
          </button>
        )}
      </div>
    </div>
  )
}