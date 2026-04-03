import { useEffect, useState, useCallback } from 'react'
import { useCallStore } from '../store/call.store'
import { useInitiateCall } from '../hooks/useInitiateCall'
import { useStringeeClient } from '../hooks/useStringeeClient'
import { StringeeSingleton } from '../services/stringee.singleton'

export const CallScreen = () => {
  const session    = useCallStore((state) => state.session)
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

  if (!session) return null
  if (!['incoming', 'outgoing', 'connected'].includes(session.status)) return null

  const formatDuration = (seconds: number) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0')
    const s = (seconds % 60).toString().padStart(2, '0')
    return `${m}:${s}`
  }

  const peerName  = session.status === 'outgoing' ? session.receiverName : session.callerName
  const isIncoming = session.status === 'incoming'

  return (
    <div
      className="fixed inset-0 z-50 bg-inverse-surface flex flex-col"
      style={isIncoming ? { visibility: 'hidden', pointerEvents: 'none', zIndex: -1 } : undefined}
    >
      {/* Remote video / voice background */}
      {session.isVideoCall ? (
        <video
          ref={remoteVideoCallbackRef}
          className="absolute inset-0 w-full h-full object-cover"
          autoPlay
          playsInline
        />
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-inverse-surface to-primary-dim flex items-center justify-center">
          <div className="w-28 h-28 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center text-5xl font-bold shadow-2xl">
            {peerName.charAt(0).toUpperCase()}
          </div>
        </div>
      )}

      {/* Dark overlay for readability */}
      <div className="absolute inset-0 bg-inverse-surface/40" />

      {/* Local video (picture-in-picture) */}
      {session.isVideoCall && (
        <video
          ref={localVideoCallbackRef}
          className="absolute top-6 right-6 w-32 h-44 object-cover rounded-2xl border-2 border-outline-variant/30 shadow-xl z-10"
          autoPlay
          playsInline
          muted
        />
      )}

      {/* Top bar */}
      <div className="relative z-10 flex flex-col items-center pt-16 gap-2">
        <p className="text-2xl font-bold text-white font-headline">{peerName}</p>
        <p className="text-sm text-white/70 font-medium">
          {session.status === 'outgoing' ? 'Đang gọi...' : formatDuration(callDurationSeconds)}
        </p>
        {session.status === 'outgoing' && (
          <div className="flex items-center gap-1.5 mt-1">
            <span className="w-1.5 h-1.5 rounded-full bg-secondary-fixed animate-pulse" />
            <span className="text-[10px] font-bold text-secondary-fixed uppercase tracking-wider">Đang kết nối</span>
          </div>
        )}
      </div>

      {/* Controls */}
      <div className="relative z-10 mt-auto mb-16 flex items-center justify-center gap-6">
        <button
          className={`w-14 h-14 rounded-full flex items-center justify-center text-white transition-all active:scale-95 ${
            isMicMuted
              ? 'bg-surface-container-high text-on-surface'
              : 'bg-white/20 backdrop-blur-sm'
          }`}
          onClick={handleToggleMic}
          aria-label={isMicMuted ? 'Bật mic' : 'Tắt mic'}
        >
          <span className="material-symbols-outlined">
            {isMicMuted ? 'mic_off' : 'mic'}
          </span>
        </button>

        <button
          className="w-16 h-16 rounded-full flex items-center justify-center bg-error-container text-on-error shadow-lg shadow-error/30 active:scale-95 transition-all"
          onClick={endCall}
          aria-label="Kết thúc cuộc gọi"
        >
          <span className="material-symbols-outlined">call_end</span>
        </button>

        {session.isVideoCall && (
          <button
            className={`w-14 h-14 rounded-full flex items-center justify-center transition-all active:scale-95 ${
              isCameraOff
                ? 'bg-surface-container-high text-on-surface'
                : 'bg-white/20 backdrop-blur-sm text-white'
            }`}
            onClick={handleToggleCamera}
            aria-label={isCameraOff ? 'Bật camera' : 'Tắt camera'}
          >
            <span className="material-symbols-outlined">
              {isCameraOff ? 'videocam_off' : 'videocam'}
            </span>
          </button>
        )}
      </div>
    </div>
  )
}