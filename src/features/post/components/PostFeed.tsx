// import { useRef, useCallback } from 'react'
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
    return <div className="post-feed__loading">Đang tải bài viết...</div>
  }

  const posts = data?.pages.flat() ?? []

  if (posts.length === 0) {
    return (
      <div className="post-feed__empty">
        <p>Chưa có bài viết nào. Hãy thêm bạn bè để xem bài viết của họ!</p>
      </div>
    )
  }

  return (
    <div className="post-feed">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} currentUserId={userId} />
      ))}

      <div ref={bottomRef} className="post-feed__bottom-trigger">
        {isFetchingNextPage && <span>Đang tải thêm...</span>}
        {!hasNextPage && posts.length > 0 && (
          <span className="post-feed__end-message">Bạn đã xem hết bài viết</span>
        )}
      </div>
    </div>
  )
}