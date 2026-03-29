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
    <div className="call-button-group">
      <button
        className="call-button call-button--voice"
        onClick={() => startCall(targetUserId, targetName, false, myName)}
        aria-label="Gọi thoại"
        title="Gọi thoại"
      >
        📞
      </button>

      <button
        className="call-button call-button--video"
        onClick={() => startCall(targetUserId, targetName, true, myName)}
        aria-label="Gọi video"
        title="Gọi video"
      >
        📹
      </button>
    </div>
  )
}