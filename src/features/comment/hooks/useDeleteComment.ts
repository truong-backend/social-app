import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { deleteCommentApi } from '../api/comment.api'
import { COMMENT_QUERY_KEYS } from '../constants/comment.constants'
import { POST_QUERY_KEYS } from '@features/post/constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'

export const useDeleteComment = (postId: string) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (commentId: string) => deleteCommentApi(commentId),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      toast.success('Đã xóa bình luận')
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}