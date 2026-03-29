import { useState } from 'react'
import type { Comment } from '../types/comment.types'
import { formatRelativeTime } from '@utils/date.formatter'

interface CommentItemProps {
  comment: Comment
  onReply?: (commentId: string) => void
  onDelete?: (commentId: string) => void
  onLike?: (commentId: string, isLiked: boolean) => void
  currentUserId: string
}

export const CommentItem = ({
  comment,
  onReply,
  onDelete,
  onLike,
  currentUserId,
}: CommentItemProps) => {
  const [showReplies, setShowReplies] = useState(false)
  const isOwner = comment.authorId === currentUserId

  return (
    <div className="comment-item">
      <img
        src={comment.authorProfilePic ?? '/default-avatar.png'}
        alt={comment.authorUsername ?? 'User'}
        className="comment-item__avatar"
      />
      <div className="comment-item__body">
        <div className="comment-item__bubble">
          <span className="comment-item__author">{comment.authorUsername ?? 'Unknown'}</span>
          <p className="comment-item__content">{comment.content}</p>
          {comment.attachedFileUrls.length > 0 && (
            <div className="comment-item__media">
              {comment.attachedFileUrls.map((url) => (
                <img key={url} src={url} alt="attachment" className="comment-item__media-img" />
              ))}
            </div>
          )}
        </div>

        <div className="comment-item__actions">
          <span className="comment-item__time">{formatRelativeTime(comment.createdAt)}</span>

          <button
            className={`comment-item__action-btn ${comment.isLiked ? 'comment-item__action-btn--active' : ''}`}
            onClick={() => onLike?.(comment.id, comment.isLiked)}
          >
            Thích {comment.likeCount > 0 && `(${comment.likeCount})`}
          </button>

          <button
            className="comment-item__action-btn"
            onClick={() => onReply?.(comment.id)}
          >
            Trả lời
          </button>

          {isOwner && (
            <button
              className="comment-item__action-btn comment-item__action-btn--danger"
              onClick={() => onDelete?.(comment.id)}
            >
              Xóa
            </button>
          )}

          {comment.replyCount > 0 && (
            <button
              className="comment-item__toggle-replies"
              onClick={() => setShowReplies((prev) => !prev)}
            >
              {showReplies ? 'Ẩn' : `Xem ${comment.replyCount} trả lời`}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}