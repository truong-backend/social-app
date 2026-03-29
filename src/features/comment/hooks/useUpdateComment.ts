import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { updateCommentApi } from '../api/comment.api'
import { COMMENT_QUERY_KEYS } from '../constants/comment.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { UpdateCommentRequest } from '../types/comment.types'

export const useUpdateComment = (postId: string) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      commentId,
      payload,
      files,
    }: {
      commentId: string
      payload:   UpdateCommentRequest
      files?:    File[]
    }) => updateCommentApi(commentId, payload, files),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
      toast.success('Đã cập nhật bình luận')
    },

    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}