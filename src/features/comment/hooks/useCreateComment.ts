import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { createCommentApi, replyCommentApi } from '../api/comment.api'
import { COMMENT_QUERY_KEYS } from '../constants/comment.constants'
import { POST_QUERY_KEYS } from '@features/post/constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { CreateCommentRequest, Comment } from '../types/comment.types'

export const useCreateComment = (postId: string) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ payload, files }: { payload: CreateCommentRequest; files?: File[] }) =>
      createCommentApi(postId, payload, files),

    onSuccess: (newComment) => {
      // Thêm comment mới vào đầu danh sách ngay lập tức (optimistic insert)
      queryClient.setQueryData<{ pages: Comment[][]; pageParams: number[] }>(
        COMMENT_QUERY_KEYS.byPost(postId),
        (old) => {
          if (!old) return old
          const firstPage = [newComment, ...(old.pages[0] ?? [])]
          return {
            ...old,
            pages: [firstPage, ...old.pages.slice(1)],
          }
        },
      )
      // Chỉ invalidate post detail để cập nhật commentCount, KHÔNG invalidate feed
      // vì feed invalidate sẽ gây remount CommentList và mất data
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}

export const useReplyComment = (postId: string, parentCommentId: string) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ payload, files }: { payload: CreateCommentRequest; files?: File[] }) =>
      replyCommentApi(postId, parentCommentId, payload, files),

    onSuccess: (newReply) => {
      // Chèn reply ngay sau parent comment trong cache
      queryClient.setQueryData<{ pages: Comment[][]; pageParams: number[] }>(
        COMMENT_QUERY_KEYS.byPost(postId),
        (old) => {
          if (!old) return old
          const newPages = old.pages.map((page) => {
            const parentIdx = page.findIndex((c) => c.id === parentCommentId)
            if (parentIdx === -1) return page
            const newPage = [...page]
            newPage.splice(parentIdx + 1, 0, newReply)
            return newPage
          })
          return { ...old, pages: newPages }
        },
      )
      // KHÔNG invalidate — invalidate sẽ reset cache và xóa reply vừa thêm
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}