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

  // Auto reject khi hết giờ ring
  useEffect(() => {
    if (session?.status !== 'incoming') return

    const timer = setTimeout(() => {
      rejectCall()
    }, CALL_RING_TIMEOUT_MS)

    return () => clearTimeout(timer)
  }, [session?.status, rejectCall])

  if (session?.status !== 'incoming') return null

  return (
    <div className="incoming-call-modal" role="dialog" aria-label="Cuộc gọi đến">
      <div className="incoming-call-modal__content">
        {/* Avatar placeholder */}
        <div className="incoming-call-modal__avatar">
          {session.callerName.charAt(0).toUpperCase()}
        </div>

        <div className="incoming-call-modal__info">
          <p className="incoming-call-modal__caller-name">{session.callerName}</p>
          <p className="incoming-call-modal__call-type">
            {session.isVideoCall ? '📹 Cuộc gọi video' : '📞 Cuộc gọi thoại'}
          </p>
        </div>

        <div className="incoming-call-modal__actions">
          <button
            className="incoming-call-modal__btn incoming-call-modal__btn--accept"
            onClick={acceptCall}
            aria-label="Chấp nhận cuộc gọi"
          >
            {session.isVideoCall ? '📹' : '📞'}
          </button>

          <button
            className="incoming-call-modal__btn incoming-call-modal__btn--reject"
            onClick={rejectCall}
            aria-label="Từ chối cuộc gọi"
          >
            📵
          </button>
        </div>
      </div>
    </div>
  )
}