import type { Post } from '../types/post.types'
import { formatRelativeTime } from '@utils/date.formatter'
import { POST_PRIVACY_LABELS } from '../constants/post.constants'

interface PostCardProps {
  post: Post
  onLike?: (postId: string) => void
  onDelete?: (postId: string) => void
  onShare?: (postId: string) => void
  currentUserId: string
}

export const PostCard = ({ post, onLike, onDelete, onShare, currentUserId }: PostCardProps) => {
  const isOwner = post.authorId === currentUserId

  return (
    <article className="post-card">
      {/* Header */}
      <header className="post-card__header">
        <img
          src={post.authorProfilePic ?? '/default-avatar.png'}
          alt={post.authorUsername ?? 'User'}
          className="post-card__avatar"
        />
        <div className="post-card__meta">
          <span className="post-card__author">{post.authorUsername ?? 'Unknown'}</span>
          <span className="post-card__time">{formatRelativeTime(post.createdAt)}</span>
          <span className="post-card__privacy">{POST_PRIVACY_LABELS[post.privacy]}</span>
        </div>
        {isOwner && (
          <button
            className="post-card__delete-btn"
            onClick={() => onDelete?.(post.id)}
            aria-label="Xóa bài viết"
          >
            ✕
          </button>
        )}
      </header>

      {/* Shared post indicator */}
      {post.isShared && post.sharedFromPostId && (
        <p className="post-card__shared-label">Đã chia sẻ bài viết</p>
      )}

      {/* Content */}
      <p className="post-card__content">{post.content}</p>

      {/* Attached files */}
      {post.attachedFileUrls.length > 0 && (
        <div className="post-card__media-grid">
          {post.attachedFileUrls.map((url) => (
            <img key={url} src={url} alt="attachment" className="post-card__media-item" />
          ))}
        </div>
      )}

      {/* Actions */}
      <footer className="post-card__footer">
        <button
          className={`post-card__action-btn ${post.isLiked ? 'post-card__action-btn--active' : ''}`}
          onClick={() => onLike?.(post.id)}
        >
          ♥ {post.likeCount}
        </button>
        <button className="post-card__action-btn">
          💬 {post.commentCount}
        </button>
        <button
          className="post-card__action-btn"
          onClick={() => onShare?.(post.id)}
        >
          ↗ {post.shareCount}
        </button>
      </footer>
    </article>
  )
}