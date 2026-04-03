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
  const { userId }    = useParams<{ userId: string }>()
  const currentUserId = useSessionStore((state) => state.userId) ?? ''
  const isOwnProfile  = userId === currentUserId

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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    )
  }

  if (isError || !profile) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
        <span className="material-symbols-outlined text-on-surface-variant text-5xl">person_off</span>
        <p className="text-on-surface-variant font-medium">Không tìm thấy người dùng</p>
      </div>
    )
  }

  const posts = postsData?.pages.flat() ?? []

  return (
    <div className="max-w-2xl mx-auto pb-24 md:pb-8">
      {/* Profile card */}
      <div className="bg-surface-container-low rounded-b-[2rem] overflow-hidden shadow-sm">
        <UserProfileCard
          profile={profile}
          isOwnProfile={isOwnProfile}
          onSendFriendRequest={() => sendRequest.mutate(profile.id)}
          onAcceptFriendRequest={() => acceptRequest.mutate(profile.id)}
          onUnfriend={() => unfriend.mutate(profile.id)}
          onBlock={() => blockUser.mutate(profile.id)}
        />
      </div>

      {isOwnProfile && (
        <div className="px-4 mt-4">
          <Link
            to="/profile/edit"
            className="flex items-center justify-center gap-2 w-full py-3 rounded-full bg-surface-container-high text-primary font-bold text-sm hover:bg-surface-container-highest transition-colors active:scale-95"
          >
            <span className="material-symbols-outlined text-sm">edit</span>
            Chỉnh sửa trang cá nhân
          </Link>
        </div>
      )}

      {/* Posts section */}
      <section className="mt-8 px-4 flex flex-col gap-4">
        <h2
          className="text-xl font-extrabold text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Bài viết
        </h2>

        {postsLoading ? (
          <div className="flex justify-center py-12"><Spinner size="md" /></div>
        ) : posts.length === 0 ? (
          <div className="flex flex-col items-center py-16 gap-3">
            <span className="material-symbols-outlined text-on-surface-variant text-4xl">article</span>
            <p className="text-on-surface-variant text-sm">Chưa có bài viết nào</p>
          </div>
        ) : (
          posts.map((post) => (
            <PostCard key={post.id} post={post} currentUserId={currentUserId} />
          ))
        )}

        <div ref={bottomRef}>
          {isFetchingNextPage && (
            <div className="flex justify-center py-4"><Spinner size="sm" /></div>
          )}
        </div>
      </section>
    </div>
  )
}