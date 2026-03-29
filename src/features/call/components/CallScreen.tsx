// import { useRef, useEffect, useState } from 'react'
import { useEffect, useState } from 'react'
import { useCallStore } from '../store/call.store'
import { useInitiateCall } from '../hooks/useInitiateCall'
import { useStringeeClient } from '../hooks/useStringeeClient'

/**
 * Màn hình cuộc gọi toàn màn hình.
 * Hiển thị khi status = 'outgoing' | 'connected'.
 * Video/Audio được xử lý qua Stringee SDK.
 */
export const CallScreen = () => {
  const session      = useCallStore((state) => state.session)
  const isMicMuted   = useCallStore((state) => state.isMicMuted)
  const isCameraOff  = useCallStore((state) => state.isCameraOff)
  const { toggleMic, toggleCamera } = useCallStore()

  const { endCall, localVideoRef, remoteVideoRef } = useInitiateCall()
  const { setMuted, setVideoEnabled } = useStringeeClient()

  const [callDurationSeconds, setCallDurationSeconds] = useState(0)

  // Đếm thời gian gọi
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
    setVideoEnabled(isCameraOff) // isCameraOff true → enable video
    toggleCamera()
  }

  if (!session || (session.status !== 'outgoing' && session.status !== 'connected')) {
    return null
  }

  const formatDuration = (seconds: number) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0')
    const s = (seconds % 60).toString().padStart(2, '0')
    return `${m}:${s}`
  }

  return (
    <div className="call-screen">
      {/* Remote video — full background */}
      {session.isVideoCall ? (
        <video
          ref={remoteVideoRef}
          className="call-screen__remote-video"
          autoPlay
          playsInline
        />
      ) : (
        // Voice call — show avatar
        <div className="call-screen__voice-bg">
          <div className="call-screen__voice-avatar">
            {session.receiverName.charAt(0).toUpperCase()}
          </div>
        </div>
      )}

      {/* Local video — small PiP */}
      {session.isVideoCall && (
        <video
          ref={localVideoRef}
          className="call-screen__local-video"
          autoPlay
          playsInline
          muted  // local stream luôn mute để tránh echo
        />
      )}

      {/* Top info bar */}
      <div className="call-screen__top-bar">
        <p className="call-screen__peer-name">
          {session.status === 'outgoing' ? session.receiverName : session.callerName}
        </p>
        <p className="call-screen__status">
          {session.status === 'outgoing'
            ? 'Đang gọi...'
            : formatDuration(callDurationSeconds)}
        </p>
      </div>

      {/* Bottom controls */}
      <div className="call-screen__controls">
        {/* Mic toggle */}
        <button
          className={`call-screen__control-btn ${isMicMuted ? 'call-screen__control-btn--active' : ''}`}
          onClick={handleToggleMic}
          aria-label={isMicMuted ? 'Bật mic' : 'Tắt mic'}
        >
          {isMicMuted ? '🔇' : '🎤'}
        </button>

        {/* End call */}
        <button
          className="call-screen__control-btn call-screen__control-btn--end"
          onClick={endCall}
          aria-label="Kết thúc cuộc gọi"
        >
          📵
        </button>

        {/* Camera toggle (video call only) */}
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