import { useEffect } from 'react'
import { useCallStore } from '../store/call.store'
import { useStringeeClient } from '../hooks/useStringeeClient'
import { useCallWebSocket } from '../hooks/useCallWebSocket'
import { IncomingCallModal } from './IncomingCallModal'
import { CallScreen } from './CallScreen'
import { CallEndedScreen } from './CallEndedScreen'

export const CallOverlay = () => {
  const status = useCallStore((state) => state.session?.status ?? 'idle')
  console.log('[CallOverlay] status:', status)
  const { initClient, disconnectClient } = useStringeeClient()

  useEffect(() => {
    initClient()
    return () => {
      disconnectClient()
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useCallWebSocket()

  if (status === 'idle') return null

  return (
    <>
      {/* 
        CallScreen render cả khi 'incoming' để video ref được bind sớm,
        tránh race condition stream đến trước khi <video> mount.
        CallScreen tự ẩn controls khi status === 'incoming'.
      */}
      {(status === 'incoming' || status === 'outgoing' || status === 'connected') && (
        <CallScreen />
      )}

      {/* IncomingCallModal đè lên trên CallScreen khi incoming */}
      {status === 'incoming' && <IncomingCallModal />}

      {status === 'ended' && <CallEndedScreen />}
    </>
  )
}