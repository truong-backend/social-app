import { useCallStore } from '../store/call.store'

/**
 * Hiển thị 2 giây sau khi cuộc gọi kết thúc trước khi clearSession.
 */
export const CallEndedScreen = () => {
  const session = useCallStore((state) => state.session)

  if (session?.status !== 'ended') return null

  return (
    <div className="call-ended-screen">
      <p className="call-ended-screen__message">Cuộc gọi đã kết thúc</p>
    </div>
  )
}