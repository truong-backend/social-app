import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import type { Comment } from '../types/comment.types'
import { formatRelativeTime } from '@utils/date.formatter'
import { getProfileApi } from '@features/user/api/user.api'

interface CommentItemProps {
  comment:       Comment
  onReply?:      (commentId: string, displayName?: string) => void
  onDelete?:     (commentId: string) => void
  onLike?:       (commentId: string, isLiked: boolean) => void
  currentUserId: string
  // parentAuthorId: id của tác giả comment cha (để hiển thị @mention trong reply)
  parentAuthorId?: string
}

export const CommentItem = ({
  comment,
  onReply,
  onDelete,
  onLike,
  currentUserId,
  parentAuthorId,
}: CommentItemProps) => {
  const isOwner = comment.authorId === currentUserId

  // Fetch profile của tác giả comment này
  const { data: authorProfile } = useQuery({
    queryKey: ['user', 'profile', comment.authorId],
    queryFn: () => getProfileApi(comment.authorId),
    staleTime: 1000 * 60 * 5,
    enabled: !!comment.authorId,
  })

  // Fetch profile của tác giả comment cha (chỉ khi là reply)
  const { data: parentAuthorProfile } = useQuery({
    queryKey: ['user', 'profile', parentAuthorId],
    queryFn: () => getProfileApi(parentAuthorId!),
    staleTime: 1000 * 60 * 5,
    enabled: !!parentAuthorId,
  })

  const displayName = authorProfile
    ? `${authorProfile.familyName ?? ''} ${authorProfile.givenName ?? ''}`.trim() ||
      authorProfile.username
    : comment.authorUsername ?? 'Unknown'

  const avatarUrl = authorProfile?.profilePictureUrl ?? comment.authorProfilePic

  // Tên người được reply (hiển thị dạng @mention)
  const parentDisplayName = parentAuthorProfile
    ? `${parentAuthorProfile.familyName ?? ''} ${parentAuthorProfile.givenName ?? ''}`.trim() ||
      parentAuthorProfile.username
    : null

  return (
    <div className="flex items-start gap-3">
      {/* Avatar */}
      <Link to={`/profile/${comment.authorId}`} className="flex-shrink-0 mt-0.5">
        {avatarUrl ? (
          <img
            src={avatarUrl}
            alt={displayName}
            className="w-8 h-8 rounded-full object-cover"
          />
        ) : (
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold text-xs">
            {displayName.charAt(0).toUpperCase()}
          </div>
        )}
      </Link>

      <div className="flex-1 min-w-0">
        {/* Bubble */}
        <div className="inline-block bg-surface-container-high/50 text-on-surface rounded-2xl rounded-tl-none px-4 py-3 max-w-full">
          {/* Tên tác giả — click vào trang profile */}
          <Link
            to={`/profile/${comment.authorId}`}
            className="text-sm font-bold text-on-surface block mb-0.5 hover:underline"
          >
            {displayName}
          </Link>

          {/* @mention người được reply — chỉ hiện nếu là reply và có thông tin */}
          {comment.repliedToCommentId && parentAuthorId && parentDisplayName && (
            <Link
              to={`/profile/${parentAuthorId}`}
              className="text-xs font-semibold text-primary hover:underline mr-1"
            >
              @{parentDisplayName}
            </Link>
          )}

          <p className="text-sm text-on-surface leading-relaxed inline">
            {comment.content}
          </p>

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
          <span className="text-[11px] text-slate-400">
            {formatRelativeTime(comment.createdAt)}
          </span>

          {onLike && (
            <button
              className={`text-[11px] font-bold transition-colors ${
                comment.isLiked ? 'text-primary' : 'text-on-surface-variant hover:text-primary'
              }`}
              onClick={() => onLike(comment.id, comment.isLiked ?? false)}
            >
              {comment.isLiked ? 'Đã thích' : 'Thích'}
            </button>
          )}

          {onReply && (
            <button
              className="text-[11px] font-bold text-on-surface-variant hover:text-primary transition-colors"
              onClick={() => onReply(comment.id, displayName)}
            >
              Trả lời
            </button>
          )}

          {isOwner && onDelete && (
            <button
              className="text-[11px] font-bold text-on-surface-variant hover:text-error transition-colors"
              onClick={() => onDelete(comment.id)}
            >
              Xóa
            </button>
          )}

          {(comment.likeCount ?? 0) > 0 && (
            <span className="text-[11px] text-on-surface-variant ml-auto flex items-center gap-1">
              <span className="w-4 h-4 bg-primary rounded-full flex items-center justify-center">
                <span
                  className="material-symbols-outlined text-white"
                  style={{ fontSize: '10px', fontVariationSettings: "'FILL' 1" }}
                >
                  favorite
                </span>
              </span>
              {comment.likeCount}
            </span>
          )}
        </div>
      </div>
    </div>
  )
}