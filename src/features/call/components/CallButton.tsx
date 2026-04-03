import { useInitiateCall } from '../hooks/useInitiateCall'
import { useMyProfile } from '@features/user/hooks/useProfile'

interface CallButtonProps {
  targetUserId: string
  targetName:   string
}

export const CallButton = ({ targetUserId, targetName }: CallButtonProps) => {
  const { data: myProfile } = useMyProfile()
  const { startCall } = useInitiateCall()

  const myName = myProfile
    ? `${myProfile.familyName} ${myProfile.givenName}`
    : 'Bạn'

  return (
    <div className="flex items-center gap-2">
      <button
        className="p-2 rounded-full text-on-surface-variant hover:bg-surface-container-high transition-colors active:scale-95 duration-200"
        onClick={() => startCall(targetUserId, targetName, false, myName)}
        aria-label="Gọi thoại"
        title="Gọi thoại"
      >
        <span className="material-symbols-outlined">call</span>
      </button>

      <button
        className="p-2 rounded-full text-on-surface-variant hover:bg-surface-container-high transition-colors active:scale-95 duration-200"
        onClick={() => startCall(targetUserId, targetName, true, myName)}
        aria-label="Gọi video"
        title="Gọi video"
      >
        <span className="material-symbols-outlined">videocam</span>
      </button>
    </div>
  )
}