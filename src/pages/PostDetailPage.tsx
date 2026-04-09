import { useParams, Link } from 'react-router-dom'
import { usePost } from '@features/post/hooks/usePost'
import { PostCard } from '@features/post/components/PostCard'
import { CommentList } from '@features/comment/components/CommentList'
import { useSessionStore } from '@stores/session.store'
import { Spinner } from '@components/feedback/Spinner'

export const PostDetailPage = () => {
  const { postId }    = useParams<{ postId: string }>()
  const currentUserId = useSessionStore((state) => state.userId) ?? ''

  const { data: post, isLoading, isError } = usePost(postId ?? '')

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    )
  }

  if (isError || !post) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
        <span className="material-symbols-outlined text-on-surface-variant text-5xl">article_shortcut</span>
        <p className="text-on-surface-variant font-medium">Không tìm thấy bài viết</p>
        <Link
          to="/feed"
          className="px-6 py-2 rounded-full bg-surface-container-high text-primary font-bold text-sm hover:bg-surface-container-highest transition-colors"
        >
          Về trang chủ
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 pb-24 md:pb-8 flex flex-col gap-6">
      {/* Back button */}
      <Link
        to="/feed"
        className="flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors w-fit group"
      >
        <span className="material-symbols-outlined text-lg group-hover:-translate-x-0.5 transition-transform">arrow_back</span>
        <span className="text-sm font-semibold">Back to Feed</span>
      </Link>

      <PostCard post={post} currentUserId={currentUserId} />

      {/* Divider */}
      <div className="flex items-center gap-4">
        <div className="flex-1 h-px bg-outline-variant/20" />
        <span className="text-xs font-bold text-on-surface-variant uppercase tracking-widest">
          Comments
        </span>
        <div className="flex-1 h-px bg-outline-variant/20" />
      </div>

      <CommentList postId={post.id} />
    </div>
  )
}
