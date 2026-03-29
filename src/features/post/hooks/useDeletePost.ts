import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { deletePostApi } from '../api/postActions.api'
import { POST_QUERY_KEYS } from '../constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'

export const useDeletePost = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (postId: string) => deletePostApi(postId),

    onSuccess: (_, postId) => {
      queryClient.removeQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
      toast.success('Đã xóa bài viết')
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}