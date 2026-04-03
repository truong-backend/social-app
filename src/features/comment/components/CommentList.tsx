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
    <div className="flex flex-col gap-4">
      {/* New comment form */}
      <form className="flex items-center gap-3 bg-surface-container-low p-2 rounded-2xl" onSubmit={handleSubmit}>
        <input
          className="flex-1 bg-transparent border-none focus:ring-0 text-sm text-on-surface placeholder:text-on-surface-variant/60 outline-none px-2"
          type="text"
          placeholder="Viết bình luận..."
          value={newCommentContent}
          onChange={(e) => setNewCommentContent(e.target.value)}
        />
        <button
          type="submit"
          className="w-9 h-9 flex items-center justify-center bg-primary text-on-primary rounded-xl shadow-md shadow-primary/20 active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none"
          disabled={createComment.isPending || !newCommentContent.trim()}
        >
          <span className="material-symbols-outlined text-sm">send</span>
        </button>
      </form>

      {/* Loading state */}
      {isLoading && (
        <div className="flex items-center justify-center py-6 gap-2 text-sm text-on-surface-variant">
          <span className="w-4 h-4 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
          Đang tải bình luận...
        </div>
      )}

      {/* Comment list */}
      <div className="flex flex-col gap-5">
        {comments.map((comment) => (
          <CommentItem
            key={comment.id}
            comment={comment}
            currentUserId={userId}
            onDelete={(commentId) => deleteComment.mutate(commentId)}
          />
        ))}
      </div>

      {/* Infinite scroll trigger */}
      <div ref={bottomRef} className="flex justify-center py-2">
        {isFetchingNextPage && (
          <div className="flex items-center gap-2 text-xs text-on-surface-variant">
            <span className="w-3.5 h-3.5 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
            Đang tải thêm...
          </div>
        )}
      </div>
    </div>
  )
}