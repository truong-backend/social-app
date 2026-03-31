import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { Post } from '../types/post.types'
import { useDeletePost } from '../hooks/useDeletePost'
import { useLikePost } from '@features/like/hooks/useLikePost'
import { formatRelativeTime } from '@utils/date.formatter'
import { POST_PRIVACY_LABELS } from '../constants/post.constants'
import { CommentList } from '@features/comment/components/CommentList'
import { SharePostModal } from './SharePostModal'

interface PostCardProps {
  post:          Post
  currentUserId: string
}

export const PostCard = ({ post, currentUserId }: PostCardProps) => {
  const [showComments, setShowComments] = useState(false)
  const [showShareModal, setShowShareModal] = useState(false)

  const isOwner  = post.authorId === currentUserId
  const likePost = useLikePost()
  const deletePost = useDeletePost()

  const handleLike = () => {
    likePost.toggle(post.id, post.isLiked)
  }

  const handleDelete = () => {
    if (window.confirm('Bạn có chắc muốn xóa bài viết này?')) {
      deletePost.mutate(post.id)
    }
  }

  return (
    <article className="post-card">
      {/* ── Header ─────────────────────────────────────── */}
      <header className="post-card__header">
        <Link to={`/profile/${post.authorId}`} className="post-card__author-link">
          <img
            src={post.authorProfilePic ?? '/default-avatar.png'}
            alt={post.authorUsername ?? 'User'}
            className="post-card__avatar"
          />
          <div className="post-card__meta">
            <span className="post-card__author">
              {post.authorUsername ?? 'Unknown'}
            </span>
            <span className="post-card__time">
              {formatRelativeTime(post.createdAt)}
            </span>
            <span className="post-card__privacy">
              {POST_PRIVACY_LABELS[post.privacy]}
            </span>
          </div>
        </Link>

        {isOwner && (
          <button
            className="post-card__delete-btn"
            onClick={handleDelete}
            disabled={deletePost.isPending}
            aria-label="Xóa bài viết"
          >
            ✕
          </button>
        )}
      </header>

      {/* ── Shared indicator ───────────────────────────── */}
      {post.isShared && post.sharedFromPostId && (
        <p className="post-card__shared-label">
          Đã chia sẻ bài viết{' '}
          <Link to={`/posts/${post.sharedFromPostId}`} className="post-card__shared-link">
            gốc
          </Link>
        </p>
      )}

      {/* ── Content ────────────────────────────────────── */}
      <Link to={`/posts/${post.id}`} className="post-card__content-link">
        <p className="post-card__content">{post.content}</p>
      </Link>

      {/* ── Attached media ─────────────────────────────── */}
      {post.attachedFileUrls.length > 0 && (
        <div className="post-card__media-grid">
          {post.attachedFileUrls.map((url) => (
            <img
              key={url}
              src={url}
              alt="attachment"
              className="post-card__media-item"
            />
          ))}
        </div>
      )}

      {/* ── Actions ────────────────────────────────────── */}
      <footer className="post-card__footer">
        {/* Like */}
        <button
          className={`post-card__action-btn ${
            post.isLiked ? 'post-card__action-btn--active' : ''
          }`}
          onClick={handleLike}
          disabled={likePost.isPending}
          aria-label={post.isLiked ? 'Bỏ thích' : 'Thích'}
        >
          {post.isLiked ? '❤️' : '🤍'} {post.likeCount}
        </button>

        {/* Comment — toggle hiện CommentList */}
        <button
          className={`post-card__action-btn ${
            showComments ? 'post-card__action-btn--active' : ''
          }`}
          onClick={() => setShowComments((prev) => !prev)}
          aria-label="Bình luận"
        >
          💬 {post.commentCount}
        </button>

        {/* Share */}
        <button
          className="post-card__action-btn"
          onClick={() => setShowShareModal(true)}
          aria-label="Chia sẻ"
        >
          ↗ {post.shareCount}
        </button>
      </footer>

      {/* ── Inline comment section ─────────────────────── */}
      {showComments && (
        <div className="post-card__comments">
          <CommentList postId={post.id} />
        </div>
      )}

      {/* ── Share modal ────────────────────────────────── */}
      {showShareModal && (
        <SharePostModal
          originalPostId={post.id}
          onClose={() => setShowShareModal(false)}
        />
      )}
    </article>
  )
}