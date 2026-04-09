import { useNavigate } from 'react-router-dom'
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
  const navigate = useNavigate()

  const handleMessageClick = () => {
    // Điều hướng sang trang messages với targetUserId dưới dạng query param
    // ChatWindow sẽ tự tạo hoặc mở chat có sẵn với người này
    navigate(`/messages?with=${profile.id}`)
  }

  const renderRelationshipButton = () => {
    if (isOwnProfile) return null
    if (profile.isFriend) {
      return (
        <button
          className="bg-surface-container-lowest text-on-surface border border-outline-variant/20 px-6 py-2.5 rounded-xl font-bold text-sm shadow-sm hover:bg-surface-container-low transition-all"
          onClick={onUnfriend}
        >
          Hủy kết bạn
        </button>
      )
    }
    if (profile.hasSentRequest) {
      return (
        <button
          className="bg-surface-container-lowest text-on-surface-variant border border-outline-variant/20 px-6 py-2.5 rounded-xl font-bold text-sm shadow-sm cursor-not-allowed opacity-70"
          disabled
        >
          Đã gửi lời mời
        </button>
      )
    }
    if (profile.hasReceivedRequest) {
      return (
        <button
          className="bg-gradient-to-br from-primary to-primary-container text-on-primary px-6 py-2.5 rounded-xl font-bold text-sm shadow-lg hover:shadow-primary/20 transition-all flex items-center gap-2"
          onClick={onAcceptFriendRequest}
        >
          <span className="material-symbols-outlined text-sm">person_add</span>
          Chấp nhận lời mời
        </button>
      )
    }
    return (
      <button
        className="bg-gradient-to-br from-primary to-primary-container text-on-primary px-6 py-2.5 rounded-xl font-bold text-sm shadow-lg hover:shadow-primary/20 transition-all flex items-center gap-2"
        onClick={onSendFriendRequest}
      >
        <span className="material-symbols-outlined text-sm">person_add</span>
        Kết bạn
      </button>
    )
  }

  return (
    <div>
      {/* Cover photo */}
      <div className="h-48 md:h-64 w-full bg-gradient-to-br from-primary to-primary-container relative overflow-hidden">
        <div className="absolute inset-0 opacity-30"
          style={{
            backgroundImage: 'radial-gradient(circle at 20% 50%, rgba(255,255,255,0.3) 0%, transparent 60%), radial-gradient(circle at 80% 20%, rgba(255,255,255,0.2) 0%, transparent 60%)'
          }}
        />
      </div>

      {/* Profile info section */}
      <div className="px-6 md:px-10 relative">
        {/* Avatar + buttons row */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 -mt-12 md:-mt-16">
          <div className="flex flex-col md:flex-row items-center md:items-end gap-4">
            {/* Avatar */}
            <div className="relative">
              <div className="w-24 h-24 md:w-36 md:h-36 rounded-full border-[5px] border-surface overflow-hidden shadow-2xl bg-surface-container-lowest">
                {profile.profilePictureUrl ? (
                  <img
                    src={profile.profilePictureUrl}
                    alt={profile.username}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full bg-gradient-to-br from-primary to-primary-container flex items-center justify-center text-on-primary text-3xl font-bold">
                    {profile.username?.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
              <div className="absolute bottom-1 right-1 w-5 h-5 bg-tertiary rounded-full border-4 border-surface shadow-[0_0_10px_rgba(0,106,38,0.3)]" />
            </div>

            {/* Name & bio */}
            <div className="text-center md:text-left mb-2">
              <h1
                className="text-2xl md:text-4xl font-extrabold tracking-tight text-on-surface"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                {profile.familyName} {profile.givenName}
              </h1>
              <p className="text-secondary font-medium mt-1">@{profile.username}</p>
              {profile.bio && (
                <p className="text-on-surface-variant max-w-md mt-2 leading-relaxed text-sm">
                  {profile.bio}
                </p>
              )}
              <p className="text-sm text-on-surface-variant mt-1">
                <span className="font-bold text-on-surface">{profile.friendCount}</span> bạn bè
              </p>
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex gap-3 mb-4 justify-center md:justify-end flex-wrap">
            {renderRelationshipButton()}

            {/* Nút Nhắn tin: chỉ hiện khi KHÔNG phải bản thân và không bị block */}
            {!isOwnProfile && !profile.isBlocked && (
              <button
                className="bg-gradient-to-br from-primary to-primary-container text-on-primary px-6 py-2.5 rounded-xl font-bold text-sm shadow-lg hover:shadow-primary/20 transition-all flex items-center gap-2"
                onClick={handleMessageClick}
              >
                <span className="material-symbols-outlined text-sm">chat</span>
                Nhắn tin
              </button>
            )}

            {!isOwnProfile && !profile.isBlocked && (
              <button
                className="bg-surface-container-lowest text-error border border-outline-variant/20 px-4 py-2.5 rounded-xl font-bold text-sm shadow-sm hover:bg-red-50 transition-all flex items-center gap-2"
                onClick={onBlock}
              >
                <span className="material-symbols-outlined text-sm">block</span>
                Chặn
              </button>
            )}
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-8 mt-4 border-b border-outline-variant/10 overflow-x-auto">
        </div>
      </div>
    </div>
  )
}