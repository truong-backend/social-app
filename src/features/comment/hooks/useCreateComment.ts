import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { createCommentApi, replyCommentApi } from '../api/comment.api'
import { COMMENT_QUERY_KEYS } from '../constants/comment.constants'
import { POST_QUERY_KEYS } from '@features/post/constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { CreateCommentRequest } from '../types/comment.types'

export const useCreateComment = (postId: string) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ payload, files }: { payload: CreateCommentRequest; files?: File[] }) =>
      createCommentApi(postId, payload, files),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
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

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.replies(parentCommentId) })
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}