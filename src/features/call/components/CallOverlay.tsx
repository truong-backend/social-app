import { useCallStore } from '../store/call.store'
import { IncomingCallModal } from './IncomingCallModal'
import { CallScreen } from './CallScreen'
import { CallEndedScreen } from './CallEndedScreen'

/**
 * Đặt ở cấp App — render đúng component theo trạng thái cuộc gọi.
 *
 * idle     → không render gì
 * incoming → IncomingCallModal (góc màn hình)
 * outgoing | connected → CallScreen (toàn màn hình)
 * ended   → CallEndedScreen (2 giây)
 */
export const CallOverlay = () => {
  const status = useCallStore((state) => state.session?.status ?? 'idle')

  if (status === 'idle') return null

  return (
    <>
      {status === 'incoming'  && <IncomingCallModal />}
      {(status === 'outgoing' || status === 'connected') && <CallScreen />}
      {status === 'ended'     && <CallEndedScreen />}
    </>
  )
}