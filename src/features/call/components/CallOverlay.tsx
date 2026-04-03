import { useCallStore } from '../store/call.store'
import { IncomingCallModal } from './IncomingCallModal'
import { CallScreen } from './CallScreen'
import { CallEndedScreen } from './CallEndedScreen'
import '../components/call.css'

export const CallOverlay = () => {
  const status = useCallStore((state) => state.session?.status ?? 'idle')

  if (status === 'idle') return null

  return (
    <>
      {(status === 'incoming' || status === 'outgoing' || status === 'connected') && (
        <CallScreen />
      )}
      {status === 'incoming' && <IncomingCallModal />}
      {status === 'ended' && <CallEndedScreen />}
    </>
  )
}