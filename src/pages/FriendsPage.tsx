import { useState } from 'react'
import {
  useFriends, useReceivedRequests, useSentRequests, useBlockedUsers,
  useAcceptFriendRequest, useDeleteRequest, useUnfriend, useUnblockUser,
} from '@features/relationship'
import { Avatar } from '@components/ui/Avatar'
import { Button } from '@components/ui/Button'
import { Spinner } from '@components/feedback/Spinner'
import { Link } from 'react-router-dom'

type FriendsTab = 'friends' | 'received' | 'sent' | 'blocked'

const TAB_META: { key: FriendsTab; label: string; icon: string }[] = [
  { key: 'friends',  label: 'Bạn bè',          icon: 'group' },
  { key: 'received', label: 'Lời mời nhận',     icon: 'person_add' },
  { key: 'sent',     label: 'Đã gửi',           icon: 'schedule_send' },
  { key: 'blocked',  label: 'Đã chặn',          icon: 'block' },
]

export const FriendsPage = () => {
  const [activeTab, setActiveTab] = useState<FriendsTab>('friends')

  const friends          = useFriends()
  const receivedRequests = useReceivedRequests()
  const sentRequests     = useSentRequests()
  const blockedUsers     = useBlockedUsers()

  const acceptRequest = useAcceptFriendRequest()
  const deleteRequest = useDeleteRequest()
  const unfriend      = useUnfriend()
  const unblock       = useUnblockUser()

  const counts: Record<FriendsTab, number | undefined> = {
    friends:  friends.data?.length,
    received: receivedRequests.data?.length,
    sent:     sentRequests.data?.length,
    blocked:  blockedUsers.data?.length,
  }

  const renderList = (
    ids:       string[],
    isLoading: boolean,
    actions:   (id: string) => React.ReactNode,
    emptyIcon: string,
    emptyText: string,
  ) => {
    if (isLoading) {
      return <div className="flex justify-center py-12"><Spinner size="md" /></div>
    }
    if (ids.length === 0) {
      return (
        <div className="flex flex-col items-center py-16 gap-3">
          <span className="material-symbols-outlined text-on-surface-variant text-4xl">{emptyIcon}</span>
          <p className="text-sm text-on-surface-variant">{emptyText}</p>
        </div>
      )
    }
    return (
      <div className="flex flex-col gap-2">
        {ids.map((id) => (
          <div
            key={id}
            className="flex items-center justify-between p-4 bg-surface-container-low rounded-2xl hover:bg-surface-container-high transition-colors"
          >
            <Link to={`/profile/${id}`} className="flex items-center gap-4 min-w-0">
              <Avatar src={null} alt={id} size="md" />
              <span className="font-semibold text-on-surface text-sm truncate">{id}</span>
            </Link>
            <div className="flex gap-2 flex-shrink-0">{actions(id)}</div>
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 pb-24 md:pb-8 flex flex-col gap-6">
      <h1
        className="text-3xl font-extrabold tracking-tight text-on-surface"
        style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
      >
        Bạn bè
      </h1>

      {/* Tabs */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-2" role="tablist">
        {TAB_META.map(({ key, label, icon }) => {
          const count = counts[key]
          const isActive = activeTab === key
          return (
            <button
              key={key}
              role="tab"
              aria-selected={isActive}
              onClick={() => setActiveTab(key)}
              className={`relative flex flex-col items-center gap-1 px-3 py-4 rounded-2xl transition-all duration-200 ${
                isActive
                  ? 'bg-surface-container-highest text-primary'
                  : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'
              }`}
            >
              <span
                className="material-symbols-outlined text-xl"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : undefined}
              >
                {icon}
              </span>
              <span className="text-xs font-bold">{label}</span>
              {!!count && count > 0 && (
                <span className="absolute top-2 right-2 min-w-[18px] h-[18px] flex items-center justify-center bg-secondary text-on-secondary rounded-full text-[10px] font-bold px-1">
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {/* Content */}
      <div>
        {activeTab === 'friends' && renderList(
          friends.data ?? [], friends.isLoading,
          (id) => (
            <Button variant="secondary" size="sm" isLoading={unfriend.isPending}
              onClick={() => unfriend.mutate(id)}>
              Hủy kết bạn
            </Button>
          ),
          'group_off', 'Chưa có bạn bè nào',
        )}

        {activeTab === 'received' && renderList(
          receivedRequests.data ?? [], receivedRequests.isLoading,
          (id) => (
            <>
              <Button variant="primary" size="sm" isLoading={acceptRequest.isPending}
                onClick={() => acceptRequest.mutate(id)}>
                Chấp nhận
              </Button>
              <Button variant="ghost" size="sm" isLoading={deleteRequest.isPending}
                onClick={() => deleteRequest.mutate(id)}>
                Từ chối
              </Button>
            </>
          ),
          'person_add_disabled', 'Không có lời mời kết bạn nào',
        )}

        {activeTab === 'sent' && renderList(
          sentRequests.data ?? [], sentRequests.isLoading,
          (id) => (
            <Button variant="ghost" size="sm" isLoading={deleteRequest.isPending}
              onClick={() => deleteRequest.mutate(id)}>
              Hủy lời mời
            </Button>
          ),
          'schedule_send', 'Chưa gửi lời mời nào',
        )}

        {activeTab === 'blocked' && renderList(
          blockedUsers.data ?? [], blockedUsers.isLoading,
          (id) => (
            <Button variant="danger" size="sm" isLoading={unblock.isPending}
              onClick={() => unblock.mutate(id)}>
              Bỏ chặn
            </Button>
          ),
          'block', 'Chưa chặn ai',
        )}
      </div>
    </div>
  )
}