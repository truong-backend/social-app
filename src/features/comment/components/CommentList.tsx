import { useState } from 'react'
import { useInView } from 'react-intersection-observer'
import { useComments } from '../hooks/useComments'
import { useCreateComment } from '../hooks/useCreateComment'
import { useDeleteComment } from '../hooks/useDeleteComment'
import { CommentItem } from './CommentItem'
import { useSessionStore } from '@stores/session.store'

interface CommentListProps {
  postId: string
}

export const CommentList = ({ postId }: CommentListProps) => {
  const [newCommentContent, setNewCommentContent] = useState('')
  const userId = useSessionStore((state) => state.userId) ?? ''

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useComments(postId)

  const createComment = useCreateComment(postId)
  const deleteComment = useDeleteComment(postId)

  const { ref: bottomRef } = useInView({
    threshold: 0,
    onChange: (inView) => {
      if (inView && hasNextPage && !isFetchingNextPage) fetchNextPage()
    },
  })

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!newCommentContent.trim()) return
    createComment.mutate(
      { payload: { content: newCommentContent } },
      { onSuccess: () => setNewCommentContent('') },
    )
  }

  const comments = data?.pages.flat() ?? []

  return (
    <div className="comment-list">
      {/* New comment input */}
      <form className="comment-list__form" onSubmit={handleSubmit}>
        <input
          className="comment-list__input"
          type="text"
          placeholder="Viết bình luận..."
          value={newCommentContent}
          onChange={(e) => setNewCommentContent(e.target.value)}
        />
        <button
          type="submit"
          className="comment-list__submit-btn"
          disabled={createComment.isPending || !newCommentContent.trim()}
        >
          Gửi
        </button>
      </form>

      {isLoading && <div className="comment-list__loading">Đang tải bình luận...</div>}

      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          comment={comment}
          currentUserId={userId}
          onDelete={(commentId) => deleteComment.mutate(commentId)}
        />
      ))}

      <div ref={bottomRef}>
        {isFetchingNextPage && <span className="comment-list__loading-more">Đang tải thêm...</span>}
      </div>
    </div>
  )
}