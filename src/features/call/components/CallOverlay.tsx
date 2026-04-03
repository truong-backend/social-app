import { useEffect } from 'react'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from '../hooks/useStringeeClient'
import { useCallWebSocket } from '../hooks/useCallWebSocket'
import { IncomingCallModal } from './IncomingCallModal'
import { CallScreen } from './CallScreen'
import { CallEndedScreen } from './CallEndedScreen'
// import '../components/call.css'
/**
 * Đặt ở cấp App — mount một lần duy nhất.
 * - Khởi tạo Stringee client khi component mount
 * - Lắng nghe WebSocket events cho call
 * - Render đúng component theo trạng thái cuộc gọi
 */
export const CallOverlay = () => {
  const status = useCallStore((state) => state.session?.status ?? 'idle')
  const { initClient, disconnectClient } = useStringeeClient()

  // FIX: Khởi tạo Stringee client khi app load
  useEffect(() => {
    initClient()
    return () => {
      disconnectClient()
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // FIX: Mount WebSocket listener cho call ở đây (App level)
  useCallWebSocket()

  if (status === 'idle') return null

  return (
    <>
      {status === 'incoming'  && <IncomingCallModal />}
      {(status === 'outgoing' || status === 'connected') && <CallScreen />}
      {status === 'ended'     && <CallEndedScreen />}
    </>
  )
}