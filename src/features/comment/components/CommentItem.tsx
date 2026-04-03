import { useState } from 'react'
import type { Comment } from '../types/comment.types'
import { formatRelativeTime } from '@utils/date.formatter'

interface CommentItemProps {
  comment:       Comment
  onReply?:      (commentId: string) => void
  onDelete?:     (commentId: string) => void
  onLike?:       (commentId: string, isLiked: boolean) => void
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
    <div className="flex items-start gap-3">
      {/* Avatar */}
      <img
        src={comment.authorProfilePic ?? '/default-avatar.png'}
        alt={comment.authorUsername ?? 'User'}
        className="w-9 h-9 rounded-full object-cover flex-shrink-0 mt-0.5"
      />

      <div className="flex-1 min-w-0">
        {/* Bubble */}
        <div className="inline-block bg-surface-container-lowest rounded-2xl rounded-tl-none px-4 py-3 shadow-[0_2px_8px_rgba(48,41,80,0.05)]">
          <span className="text-sm font-bold text-on-surface block mb-1">
            {comment.authorUsername ?? 'Unknown'}
          </span>
          <p className="text-sm text-on-surface leading-relaxed">{comment.content}</p>

          {comment.attachedFileUrls.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {comment.attachedFileUrls.map((url) => (
                <img
                  key={url}
                  src={url}
                  alt="attachment"
                  className="rounded-xl max-w-[180px] border border-outline-variant/10"
                />
              ))}
            </div>
          )}
        </div>

        {/* Actions row */}
        <div className="flex items-center gap-4 mt-1.5 px-1">
          <span className="text-[11px] text-on-surface-variant">
            {formatRelativeTime(comment.createdAt)}
          </span>

          <button
            className={`text-xs font-semibold transition-colors ${
              comment.isLiked ? 'text-secondary' : 'text-on-surface-variant hover:text-secondary'
            }`}
            onClick={() => onLike?.(comment.id, comment.isLiked)}
          >
            {comment.isLiked ? '❤️' : '🤍'} Thích
            {comment.likeCount > 0 && (
              <span className="ml-1 text-on-surface-variant">({comment.likeCount})</span>
            )}
          </button>

          <button
            className="text-xs font-semibold text-on-surface-variant hover:text-primary transition-colors"
            onClick={() => onReply?.(comment.id)}
          >
            Trả lời
          </button>

          {isOwner && (
            <button
              className="text-xs font-semibold text-on-surface-variant hover:text-error transition-colors"
              onClick={() => onDelete?.(comment.id)}
            >
              Xóa
            </button>
          )}
        </div>

        {/* Toggle replies */}
        {comment.replyCount > 0 && (
          <button
            className="mt-1 ml-1 flex items-center gap-1 text-xs font-bold text-primary hover:underline transition-colors"
            onClick={() => setShowReplies((prev) => !prev)}
          >
            <span className="material-symbols-outlined text-sm">
              {showReplies ? 'expand_less' : 'expand_more'}
            </span>
            {showReplies ? 'Ẩn trả lời' : `Xem ${comment.replyCount} trả lời`}
          </button>
        )}
      </div>
    </div>
  )
}