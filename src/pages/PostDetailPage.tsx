import { useParams } from 'react-router-dom'
import { usePost } from '@features/post/hooks/usePost'
import { PostCard } from '@features/post/components/PostCard'
import { CommentList } from '@features/comment/components/CommentList'
import { useSessionStore } from '@stores/session.store'
import { Spinner } from '@components/feedback/Spinner'

export const PostDetailPage = () => {
  const { postId }   = useParams<{ postId: string }>()
  const currentUserId = useSessionStore((state) => state.userId) ?? ''

  const { data: post, isLoading, isError } = usePost(postId ?? '')

  if (isLoading) {
    return <div className="post-detail-page__loading"><Spinner size="lg" /></div>
  }

  if (isError || !post) {
    return (
      <div className="post-detail-page__error">
        <p>Không tìm thấy bài viết</p>
      </div>
    )
  }

  return (
    <div className="post-detail-page">
      <PostCard post={post} currentUserId={currentUserId} />
      <CommentList postId={post.id} />
    </div>
  )
}