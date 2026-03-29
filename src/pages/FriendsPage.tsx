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

  const tabs: { key: FriendsTab; label: string; count?: number }[] = [
    { key: 'friends',  label: 'Bạn bè',            count: friends.data?.length },
    { key: 'received', label: 'Lời mời nhận',       count: receivedRequests.data?.length },
    { key: 'sent',     label: 'Lời mời đã gửi',     count: sentRequests.data?.length },
    { key: 'blocked',  label: 'Đã chặn',            count: blockedUsers.data?.length },
  ]

  const renderList = (
    ids:       string[],
    isLoading: boolean,
    actions:   (id: string) => React.ReactNode,
    emptyText: string,
  ) => {
    if (isLoading) return <div className="friends-page__loading"><Spinner size="md" /></div>
    if (ids.length === 0) return <p className="friends-page__empty">{emptyText}</p>

    return (
      <div className="friends-page__list">
        {ids.map((id) => (
          <div key={id} className="friends-page__item">
            <Link to={`/profile/${id}`} className="friends-page__item-link">
              <Avatar src={null} alt={id} size="md" />
              <span className="friends-page__item-id">{id}</span>
            </Link>
            <div className="friends-page__item-actions">{actions(id)}</div>
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="friends-page">
      <h1 className="friends-page__title">Bạn bè</h1>

      <div className="friends-page__tabs" role="tablist">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            role="tab"
            aria-selected={activeTab === tab.key}
            className={`friends-page__tab ${activeTab === tab.key ? 'friends-page__tab--active' : ''}`}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
            {!!tab.count && tab.count > 0 && (
              <span className="friends-page__tab-badge">{tab.count}</span>
            )}
          </button>
        ))}
      </div>

      <div className="friends-page__content">
        {activeTab === 'friends' && renderList(
          friends.data ?? [], friends.isLoading,
          (id) => (
            <Button variant="secondary" size="sm" isLoading={unfriend.isPending}
              onClick={() => unfriend.mutate(id)}>
              Hủy kết bạn
            </Button>
          ),
          'Chưa có bạn bè nào',
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
          'Không có lời mời kết bạn nào',
        )}

        {activeTab === 'sent' && renderList(
          sentRequests.data ?? [], sentRequests.isLoading,
          (id) => (
            <Button variant="ghost" size="sm" isLoading={deleteRequest.isPending}
              onClick={() => deleteRequest.mutate(id)}>
              Hủy lời mời
            </Button>
          ),
          'Chưa gửi lời mời nào',
        )}

        {activeTab === 'blocked' && renderList(
          blockedUsers.data ?? [], blockedUsers.isLoading,
          (id) => (
            <Button variant="danger" size="sm" isLoading={unblock.isPending}
              onClick={() => unblock.mutate(id)}>
              Bỏ chặn
            </Button>
          ),
          'Chưa chặn ai',
        )}
      </div>
    </div>
  )
}