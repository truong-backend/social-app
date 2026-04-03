import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { Post } from '../types/post.types'
import { useDeletePost } from '../hooks/useDeletePost'
import { useLikePost } from '@features/like/hooks/useLikePost'
import { formatRelativeTime } from '@utils/date.formatter'
import { CommentList } from '@features/comment/components/CommentList'
import { SharePostModal } from './SharePostModal'

interface PostCardProps {
  post: Post
  currentUserId: string
}

export const PostCard = ({ post, currentUserId }: PostCardProps) => {
  const [showComments, setShowComments] = useState(false)
  const [showShareModal, setShowShareModal] = useState(false)
  const isOwner = post.authorId === currentUserId
  const likePost = useLikePost()
  const deletePost = useDeletePost()

  return (
    <article className="bg-surface-container-low rounded-xl p-4 shadow-sm mb-6">

      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <Link to={`/profile/${post.authorId}`} className="flex items-center gap-3">
          <img
            src={post.authorProfilePic ?? '/default-avatar.png'}
            alt={post.authorUsername ?? 'User'}
            className="w-10 h-10 rounded-full object-cover"
          />
          <div>
            <h4 className="font-bold text-on-surface">{post.authorUsername ?? 'Unknown'}</h4>
            <span className="text-[0.6875rem] text-on-surface-variant">
              {formatRelativeTime(post.createdAt)}
            </span>
          </div>
        </Link>
        {isOwner && (
          <button
            onClick={() => window.confirm('Xóa bài viết?') && deletePost.mutate(post.id)}
            disabled={deletePost.isPending}
            className="text-on-surface-variant hover:text-error transition-colors"
          >
            <span className="material-symbols-outlined">more_horiz</span>
          </button>
        )}
      </div>

      {/* Shared label */}
      {post.isShared && post.sharedFromPostId && (
        <p className="text-sm text-on-surface-variant mb-2">
          Đã chia sẻ{' '}
          <Link to={`/posts/${post.sharedFromPostId}`} className="text-primary underline">
            bài viết gốc
          </Link>
        </p>
      )}

      {/* Content */}
      <Link to={`/posts/${post.id}`}>
        <p className="text-on-surface leading-relaxed mb-4">{post.content}</p>
      </Link>

      {/* Media */}
      {post.attachedFileUrls.length > 0 && (
        <div className={`grid gap-2 mb-4 rounded-xl overflow-hidden ${
          post.attachedFileUrls.length === 1 ? 'grid-cols-1' : 'grid-cols-2'
        }`}>
          {post.attachedFileUrls.map((url) => (
            <img key={url} src={url} alt="attachment" className="w-full h-64 object-cover" />
          ))}
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between mt-2">
        <div className="flex items-center gap-6">
          {/* Like */}
          <button
            onClick={() => likePost.toggle(post.id, post.isLiked)}
            disabled={likePost.isPending}
            className={`flex items-center gap-1.5 hover:scale-110 transition-transform ${
              post.isLiked ? 'text-secondary' : 'text-on-surface-variant'
            }`}
          >
            <span
              className="material-symbols-outlined"
              style={post.isLiked ? { fontVariationSettings: "'FILL' 1" } : undefined}
            >
              favorite
            </span>
            <span className="font-semibold text-sm">{post.likeCount}</span>
          </button>

          {/* Comment */}
          <button
            onClick={() => setShowComments(p => !p)}
            className="flex items-center gap-1.5 text-primary hover:scale-110 transition-transform"
          >
            <span className="material-symbols-outlined">chat_bubble</span>
            <span className="font-semibold text-sm">{post.commentCount}</span>
          </button>

          {/* Share */}
          <button
            onClick={() => setShowShareModal(true)}
            className="flex items-center gap-1.5 text-on-surface-variant hover:scale-110 transition-transform"
          >
            <span className="material-symbols-outlined">share</span>
            <span className="font-semibold text-sm">{post.shareCount}</span>
          </button>
        </div>

        <button className="text-on-surface-variant hover:scale-110 transition-transform">
          <span className="material-symbols-outlined">bookmark</span>
        </button>
      </div>

      {/* Comments */}
      {showComments && (
        <div className="mt-4 pt-4 border-t border-outline-variant/20">
          <CommentList postId={post.id} />
        </div>
      )}

      {/* Share Modal */}
      {showShareModal && (
        <SharePostModal originalPostId={post.id} onClose={() => setShowShareModal(false)} />
      )}
    </article>
  )
}