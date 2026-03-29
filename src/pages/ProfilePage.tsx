import { useParams, Link } from 'react-router-dom'
import { useInView } from 'react-intersection-observer'
import { useProfile } from '@features/user/hooks/useProfile'
import { UserProfileCard } from '@features/user/components/UserProfileCard'
import { PostCard } from '@features/post/components/PostCard'
import { usePostsByAuthor } from '@features/post/hooks/usePostsByAuthor'
import { useSessionStore } from '@stores/session.store'
import { Spinner } from '@components/feedback/Spinner'
import {
  useSendFriendRequest,
  useAcceptFriendRequest,
  useUnfriend,
  useBlockUser,
} from '@features/relationship'

export const ProfilePage = () => {
  const { userId }     = useParams<{ userId: string }>()
  const currentUserId  = useSessionStore((state) => state.userId) ?? ''
  const isOwnProfile   = userId === currentUserId

  const { data: profile, isLoading, isError } = useProfile(userId ?? '')

  const sendRequest   = useSendFriendRequest()
  const acceptRequest = useAcceptFriendRequest()
  const unfriend      = useUnfriend()
  const blockUser     = useBlockUser()

  const {
    data: postsData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading: postsLoading,
  } = usePostsByAuthor(userId ?? '', currentUserId)

  const { ref: bottomRef } = useInView({
    threshold: 0,
    onChange: (inView) => {
      if (inView && hasNextPage && !isFetchingNextPage) fetchNextPage()
    },
  })

  if (isLoading) return <div className="profile-page__loading"><Spinner size="lg" /></div>
  if (isError || !profile) return <div className="profile-page__error"><p>Không tìm thấy người dùng</p></div>

  const posts = postsData?.pages.flat() ?? []

  return (
    <div className="profile-page">
      <UserProfileCard
        profile={profile}
        isOwnProfile={isOwnProfile}
        onSendFriendRequest={() => sendRequest.mutate(profile.id)}
        onAcceptFriendRequest={() => acceptRequest.mutate(profile.id)}
        onUnfriend={() => unfriend.mutate(profile.id)}
        onBlock={() => blockUser.mutate(profile.id)}
      />

      {isOwnProfile && (
        <Link to="/profile/edit" className="profile-page__edit-btn">
          Chỉnh sửa trang cá nhân
        </Link>
      )}

      {/* Post list */}
      <section className="profile-page__posts">
        <h2 className="profile-page__posts-title">Bài viết</h2>

        {postsLoading ? (
          <Spinner size="md" />
        ) : posts.length === 0 ? (
          <p className="profile-page__posts-empty">Chưa có bài viết nào</p>
        ) : (
          posts.map((post) => (
            <PostCard key={post.id} post={post} currentUserId={currentUserId} />
          ))
        )}

        <div ref={bottomRef}>
          {isFetchingNextPage && <Spinner size="sm" />}
        </div>
      </section>
    </div>
  )
}