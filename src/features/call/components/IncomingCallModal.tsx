import { useEffect } from 'react'
import { useCallStore } from '../store/call.store'
import { useAnswerCall } from '../hooks/useAnswerCall'
import { CALL_RING_TIMEOUT_MS } from '../constants/call.constants'

/**
 * Modal hiện ở góc màn hình khi có cuộc gọi đến.
 * Auto-reject sau CALL_RING_TIMEOUT_MS nếu không bắt máy.
 */
export const IncomingCallModal = () => {
  const session = useCallStore((state) => state.session)
  const { acceptCall, rejectCall } = useAnswerCall()

  useEffect(() => {
    if (session?.status !== 'incoming') return
    const timer = setTimeout(() => {
      rejectCall()
    }, CALL_RING_TIMEOUT_MS)
    return () => clearTimeout(timer)
  }, [session?.status, rejectCall])

  if (session?.status !== 'incoming') return null

  return (
    <div
      className="fixed bottom-24 right-6 z-50 w-80 rounded-2xl bg-surface-container-lowest shadow-2xl shadow-inverse-surface/20 overflow-hidden"
      role="dialog"
      aria-label="Cuộc gọi đến"
    >
      {/* Gradient accent bar */}
      <div className="h-1 w-full bg-gradient-to-r from-primary to-tertiary" />

      <div className="p-5 flex items-center gap-4">
        {/* Avatar */}
        <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center text-lg font-bold flex-shrink-0 shadow-lg shadow-primary/30">
          {session.callerName.charAt(0).toUpperCase()}
        </div>

        <div className="flex-1 min-w-0">
          <p className="font-bold text-on-surface truncate">{session.callerName}</p>
          <p className="text-xs text-on-surface-variant mt-0.5">
            {session.isVideoCall ? 'Cuộc gọi video đến' : 'Cuộc gọi thoại đến'}
          </p>
          {/* Pulsing indicator */}
          <div className="flex items-center gap-1.5 mt-1">
            <span className="w-1.5 h-1.5 rounded-full bg-secondary animate-pulse" />
            <span className="text-[10px] font-bold text-secondary uppercase tracking-wider">Đang đổ chuông</span>
          </div>
        </div>
      </div>

      <div className="px-5 pb-5 flex items-center gap-3">
        <button
          className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary text-sm font-bold shadow-md shadow-primary/20 active:scale-95 transition-all"
          onClick={acceptCall}
          aria-label="Chấp nhận cuộc gọi"
        >
          <span className="material-symbols-outlined text-sm">
            {session.isVideoCall ? 'videocam' : 'call'}
          </span>
          Bắt máy
        </button>

        <button
          className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-full bg-error-container text-on-error text-sm font-bold active:scale-95 transition-all"
          onClick={rejectCall}
          aria-label="Từ chối cuộc gọi"
        >
          <span className="material-symbols-outlined text-sm">call_end</span>
          Từ chối
        </button>
      </div>
    </div>
  )
}