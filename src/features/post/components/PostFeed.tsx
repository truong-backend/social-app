import { useInView } from 'react-intersection-observer'
import { useInfiniteFeed } from '../hooks/useInfiniteFeed'
import { PostCard } from './PostCard'
import { useSessionStore } from '@stores/session.store'

export const PostFeed = () => {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useInfiniteFeed()
  const userId = useSessionStore((state) => state.userId) ?? ''

  const { ref: bottomRef } = useInView({
    threshold: 0,
    onChange: (inView) => {
      if (inView && hasNextPage && !isFetchingNextPage) {
        fetchNextPage()
      }
    },
  })

  if (isLoading) {
    return (
      <div className="flex flex-col gap-6">
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-surface-container-lowest rounded-xl p-6 shadow-sm animate-pulse">
            <div className="flex gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-surface-container-high" />
              <div className="flex-1 space-y-2">
                <div className="h-3 bg-surface-container-high rounded w-1/3" />
                <div className="h-2 bg-surface-container-high rounded w-1/5" />
              </div>
            </div>
            <div className="space-y-2">
              <div className="h-3 bg-surface-container-high rounded" />
              <div className="h-3 bg-surface-container-high rounded w-4/5" />
            </div>
          </div>
        ))}
      </div>
    )
  }

  const posts = data?.pages.flat() ?? []

  if (posts.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-4 bg-surface-container-lowest rounded-xl shadow-sm">
        <span className="material-symbols-outlined text-on-surface-variant text-5xl opacity-40">
          dynamic_feed
        </span>
        <div className="text-center">
          <p className="font-bold text-on-surface" style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
            No posts yet
          </p>
          <p className="text-sm text-on-surface-variant mt-1">
            Add friends to see their posts here!
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} currentUserId={userId} />
      ))}

      <div ref={bottomRef} className="flex justify-center py-8">
        {isFetchingNextPage && (
          <div className="flex items-center gap-2 text-slate-400">
            <span className="w-2 h-2 bg-primary/20 rounded-full animate-pulse" />
            <span className="w-2 h-2 bg-primary/40 rounded-full animate-pulse" style={{ animationDelay: '0.2s' }} />
            <span className="w-2 h-2 bg-primary/20 rounded-full animate-pulse" style={{ animationDelay: '0.4s' }} />
          </div>
        )}
        {!hasNextPage && posts.length > 0 && (
          <div className="flex items-center gap-2 text-slate-400">
            <div className="w-2 h-2 bg-primary/20 rounded-full" />
            <span className="text-xs font-medium">You're all caught up</span>
            <div className="w-2 h-2 bg-primary/20 rounded-full" />
          </div>
        )}
      </div>
    </div>
  )
}
