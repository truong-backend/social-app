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
    <div className="min-h-screen bg-surface pb-24 md:pb-8">
      {/* Profile header card */}
      <div className="bg-surface-container-lowest shadow-sm">
        <UserProfileCard
          profile={profile}
          isOwnProfile={isOwnProfile}
          onSendFriendRequest={() => sendRequest.mutate(profile.id)}
          onAcceptFriendRequest={() => acceptRequest.mutate(profile.id)}
          onUnfriend={() => unfriend.mutate(profile.id)}
          onBlock={() => blockUser.mutate(profile.id)}
        />
      </div>

      {/* Content grid */}
      <div className="max-w-6xl mx-auto px-4 md:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left sidebar */}
          <aside className="lg:col-span-4 space-y-6">
            {/* Quick Stats */}
            <div className="bg-surface-container-lowest p-6 rounded-xl shadow-sm">
              <h2
                className="text-lg font-bold mb-4"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                Quick Stats
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-surface-container-low p-4 rounded-lg">
                  <span className="text-xs text-on-surface-variant font-bold uppercase tracking-wider">Followers</span>
                  <p className="text-2xl font-extrabold text-primary">{profile.friendCount ?? 0}</p>
                </div>
                <div className="bg-surface-container-low p-4 rounded-lg">
                  <span className="text-xs text-on-surface-variant font-bold uppercase tracking-wider">Following</span>
                  <p className="text-2xl font-extrabold text-primary">0</p>
                </div>
              </div>
            </div>

            {/* Intro Info */}
            <div className="bg-surface-container-lowest p-6 rounded-xl shadow-sm">
              <h2
                className="text-lg font-bold mb-4"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                Intro
              </h2>
              <ul className="space-y-4">
                {profile.bio && (
                  <li className="flex items-center gap-3 text-on-surface-variant">
                    <span className="material-symbols-outlined text-primary text-xl">info</span>
                    <span className="text-sm">{profile.bio}</span>
                  </li>
                )}
                <li className="flex items-center gap-3 text-on-surface-variant">
                  <span className="material-symbols-outlined text-primary text-xl">person</span>
                  <span className="text-sm">@{profile.username}</span>
                </li>
              </ul>

              {isOwnProfile && (
                <Link
                  to="/profile/edit"
                  className="flex items-center justify-center gap-2 w-full mt-6 py-2.5 rounded-xl bg-surface-container-low text-on-surface font-bold text-sm hover:bg-surface-container transition-colors"
                >
                  <span className="material-symbols-outlined text-sm">edit</span>
                  Edit Profile
                </Link>
              )}
            </div>
          </aside>

          {/* Right feed column */}
          <main className="lg:col-span-8 space-y-6">
            {postsLoading ? (
              <div className="flex justify-center py-12">
                <Spinner size="md" />
              </div>
            ) : posts.length === 0 ? (
              <div className="flex flex-col items-center py-16 gap-3 bg-surface-container-lowest rounded-xl shadow-sm">
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
                <div className="flex justify-center py-4">
                  <Spinner size="sm" />
                </div>
              )}
            </div>
          </main>
        </div>
      </div>
    </div>
  )
}
