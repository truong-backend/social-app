import { useState, useRef } from 'react'
import { useInView } from 'react-intersection-observer'
import { useComments } from '../hooks/useComments'
import { useCreateComment, useReplyComment } from '../hooks/useCreateComment'
import { useDeleteComment } from '../hooks/useDeleteComment'
import { useLikeComment } from '@features/like/hooks/useLikeComment'
import { CommentItem } from './CommentItem'
import { useSessionStore } from '@stores/session.store'

interface CommentListProps {
  postId: string
}

export const CommentList = ({ postId }: CommentListProps) => {
  const [newCommentContent, setNewCommentContent] = useState('')
  const [newCommentFiles, setNewCommentFiles]     = useState<File[]>([])
  const [replyingTo, setReplyingTo]               = useState<{ id: string; username: string; displayName: string } | null>(null)
  const [replyContent, setReplyContent]           = useState('')
  const [replyFiles, setReplyFiles]               = useState<File[]>([])

  const commentFileRef = useRef<HTMLInputElement>(null)
  const replyFileRef   = useRef<HTMLInputElement>(null)

  const userId = useSessionStore((state) => state.userId) ?? ''

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useComments(postId)

  const createComment = useCreateComment(postId)
  const deleteComment = useDeleteComment(postId)
  const likeComment   = useLikeComment(postId)
  const replyComment  = useReplyComment(postId, replyingTo?.id ?? '')

  const { ref: bottomRef } = useInView({
    threshold: 0,
    onChange: (inView) => {
      if (inView && hasNextPage && !isFetchingNextPage) fetchNextPage()
    },
  })

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!newCommentContent.trim() && newCommentFiles.length === 0) return
    createComment.mutate(
      { payload: { content: newCommentContent }, files: newCommentFiles },
      {
        onSuccess: () => {
          setNewCommentContent('')
          setNewCommentFiles([])
        },
      },
    )
  }

  const handleReplySubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!replyContent.trim() && replyFiles.length === 0) return
    if (!replyingTo) return
    replyComment.mutate(
      { payload: { content: replyContent }, files: replyFiles },
      {
        onSuccess: () => {
          setReplyContent('')
          setReplyFiles([])
          setReplyingTo(null)
        },
      },
    )
  }

  const removeCommentFile = (idx: number) =>
    setNewCommentFiles((prev) => prev.filter((_, i) => i !== idx))

  const removeReplyFile = (idx: number) =>
    setReplyFiles((prev) => prev.filter((_, i) => i !== idx))

  const comments = data?.pages.flat() ?? []

  return (
    <div className="flex flex-col gap-4 mt-4">

      {/* New comment form */}
      <form
        className="flex flex-col gap-2 bg-surface-container-lowest rounded-2xl p-2 border border-outline-variant/10 shadow-sm"
        onSubmit={handleSubmit}
      >
        {newCommentFiles.length > 0 && (
          <div className="flex flex-wrap gap-2 px-2 pt-1">
            {newCommentFiles.map((f, i) => (
              <div key={i} className="relative">
                <img
                  src={URL.createObjectURL(f)}
                  alt={f.name}
                  className="w-16 h-16 object-cover rounded-xl border border-outline-variant/20"
                />
                <button
                  type="button"
                  onClick={() => removeCommentFile(i)}
                  className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-error text-white rounded-full flex items-center justify-center text-[9px] leading-none"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        )}

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => commentFileRef.current?.click()}
            className="w-8 h-8 flex-shrink-0 flex items-center justify-center text-on-surface-variant hover:text-primary rounded-xl transition-colors"
            title="Đính kèm ảnh"
          >
            <span className="material-symbols-outlined text-lg">image</span>
          </button>
          <input
            ref={commentFileRef}
            type="file"
            accept="image/*"
            multiple
            className="hidden"
            onChange={(e) => {
              const files = Array.from(e.target.files ?? [])
              setNewCommentFiles((prev) => [...prev, ...files])
              e.target.value = ''
            }}
          />

          <input
            className="flex-1 bg-transparent border-none focus:ring-0 text-sm text-on-surface placeholder:text-on-surface-variant/60 outline-none px-2"
            type="text"
            placeholder="Viết bình luận..."
            value={newCommentContent}
            onChange={(e) => setNewCommentContent(e.target.value)}
          />

          <button
            type="submit"
            className="w-8 h-8 flex-shrink-0 flex items-center justify-center bg-primary text-on-primary rounded-xl shadow-md shadow-primary/20 active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none"
            disabled={createComment.isPending || (!newCommentContent.trim() && newCommentFiles.length === 0)}
          >
            {createComment.isPending ? (
              <span className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
            ) : (
              <span className="material-symbols-outlined text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>
                send
              </span>
            )}
          </button>
        </div>
      </form>

      {/* Loading state */}
      {isLoading && (
        <div className="flex items-center justify-center py-6 gap-2 text-sm text-on-surface-variant">
          <span className="w-4 h-4 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
          Đang tải bình luận...
        </div>
      )}

      {/* Comment list */}
      <div className="flex flex-col gap-4">
        {comments.map((comment) => {
          // Tìm authorId của comment cha để CommentItem hiển thị @mention
          const parentAuthorId = comment.repliedToCommentId
            ? comments.find((c) => c.id === comment.repliedToCommentId)?.authorId
            : undefined

          return (
          <div key={comment.id} className={comment.repliedToCommentId ? 'ml-11' : ''}>
            <CommentItem
              comment={comment}
              currentUserId={userId}
              parentAuthorId={parentAuthorId}
              onDelete={(commentId) => deleteComment.mutate(commentId)}
              onLike={(commentId, isLiked) => likeComment.toggle(commentId, isLiked)}
              onReply={(commentId, displayName) => {
                setReplyingTo({
                  id: commentId,
                  username: comment.authorUsername ?? 'người dùng',
                  displayName: displayName ?? comment.authorUsername ?? 'người dùng',
                })
                setReplyContent('')
                setReplyFiles([])
              }}
            />

            {/* Reply form */}
            {replyingTo?.id === comment.id && (
              <form
                className="ml-11 mt-2 flex flex-col gap-2 bg-primary/5 rounded-2xl p-2 border border-primary/20"
                onSubmit={handleReplySubmit}
              >
                {replyFiles.length > 0 && (
                  <div className="flex flex-wrap gap-2 px-2 pt-1">
                    {replyFiles.map((f, i) => (
                      <div key={i} className="relative">
                        <img
                          src={URL.createObjectURL(f)}
                          alt={f.name}
                          className="w-14 h-14 object-cover rounded-xl border border-outline-variant/20"
                        />
                        <button
                          type="button"
                          onClick={() => removeReplyFile(i)}
                          className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-error text-white rounded-full flex items-center justify-center text-[9px] leading-none"
                        >
                          ✕
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => replyFileRef.current?.click()}
                    className="w-7 h-7 flex-shrink-0 flex items-center justify-center text-primary/60 hover:text-primary rounded-lg transition-colors"
                    title="Đính kèm ảnh"
                  >
                    <span className="material-symbols-outlined text-base">image</span>
                  </button>
                  <input
                    ref={replyFileRef}
                    type="file"
                    accept="image/*"
                    multiple
                    className="hidden"
                    onChange={(e) => {
                      const files = Array.from(e.target.files ?? [])
                      setReplyFiles((prev) => [...prev, ...files])
                      e.target.value = ''
                    }}
                  />

                  <div className="flex-1 flex flex-col">
                    <span className="text-[10px] text-primary font-semibold px-2 mb-0.5">
                      Trả lời {replyingTo.displayName}
                    </span>
                    <input
                      className="bg-transparent border-none focus:ring-0 text-sm text-on-surface placeholder:text-on-surface-variant/60 outline-none px-2"
                      type="text"
                      placeholder={`Trả lời ${replyingTo.displayName}...`}
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      autoFocus
                    />
                  </div>

                  <button
                    type="button"
                    onClick={() => { setReplyingTo(null); setReplyContent(''); setReplyFiles([]) }}
                    className="w-7 h-7 flex-shrink-0 flex items-center justify-center text-on-surface-variant hover:text-error rounded-lg transition-colors"
                  >
                    <span className="material-symbols-outlined text-sm">close</span>
                  </button>

                  <button
                    type="submit"
                    className="w-8 h-8 flex-shrink-0 flex items-center justify-center bg-primary text-on-primary rounded-xl shadow-md active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none"
                    disabled={replyComment.isPending || (!replyContent.trim() && replyFiles.length === 0)}
                  >
                    {replyComment.isPending ? (
                      <span className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                    ) : (
                      <span className="material-symbols-outlined text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>
                        send
                      </span>
                    )}
                  </button>
                </div>
              </form>
            )}
          </div>
          )
        })}
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