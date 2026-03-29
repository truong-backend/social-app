import type { UserProfile } from '../types/user.types'
import { UserAvatar } from './UserAvatar'

interface UserProfileCardProps {
  profile: UserProfile
  isOwnProfile: boolean
  onSendFriendRequest?: () => void
  onAcceptFriendRequest?: () => void
  onUnfriend?: () => void
  onBlock?: () => void
}

export const UserProfileCard = ({
  profile,
  isOwnProfile,
  onSendFriendRequest,
  onAcceptFriendRequest,
  onUnfriend,
  onBlock,
}: UserProfileCardProps) => {
  const renderRelationshipButton = () => {
    if (isOwnProfile) return null
    if (profile.isFriend) {
      return (
        <button className="profile-card__btn profile-card__btn--secondary" onClick={onUnfriend}>
          Hủy kết bạn
        </button>
      )
    }
    if (profile.hasSentRequest) {
      return (
        <button className="profile-card__btn profile-card__btn--secondary" disabled>
          Đã gửi lời mời
        </button>
      )
    }
    if (profile.hasReceivedRequest) {
      return (
        <button className="profile-card__btn profile-card__btn--primary" onClick={onAcceptFriendRequest}>
          Chấp nhận lời mời
        </button>
      )
    }
    return (
      <button className="profile-card__btn profile-card__btn--primary" onClick={onSendFriendRequest}>
        Kết bạn
      </button>
    )
  }

  return (
    <div className="profile-card">
      <UserAvatar
        src={profile.profilePictureUrl}
        username={profile.username}
        size="lg"
        className="profile-card__avatar"
      />

      <div className="profile-card__info">
        <h1 className="profile-card__name">
          {profile.familyName} {profile.givenName}
        </h1>
        <p className="profile-card__username">@{profile.username}</p>
        {profile.bio && <p className="profile-card__bio">{profile.bio}</p>}
        <p className="profile-card__friends">{profile.friendCount} bạn bè</p>
      </div>

      <div className="profile-card__actions">
        {renderRelationshipButton()}
        {!isOwnProfile && !profile.isBlocked && (
          <button
            className="profile-card__btn profile-card__btn--danger"
            onClick={onBlock}
          >
            Chặn
          </button>
        )}
      </div>
    </div>
  )
}