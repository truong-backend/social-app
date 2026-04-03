import { useCallStore } from '../store/call.store'

/**
 * Hiển thị 2 giây sau khi cuộc gọi kết thúc trước khi clearSession.
 */
export const CallEndedScreen = () => {
  const session = useCallStore((state) => state.session)

  if (session?.status !== 'ended') return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/80 backdrop-blur-sm">
      <div className="flex flex-col items-center gap-3 px-8 py-6 rounded-2xl bg-surface-container-lowest shadow-2xl">
        <span className="text-4xl">📵</span>
        <p className="text-base font-semibold text-on-surface">Cuộc gọi đã kết thúc</p>
      </div>
    </div>
  )
}