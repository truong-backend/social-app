import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { createPostApi } from '../api/createPost.api'
import { POST_QUERY_KEYS } from '../constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { CreatePostRequest } from '../types/post.types'

export const useCreatePost = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ payload, files }: { payload: CreatePostRequest; files?: File[] }) =>
      createPostApi(payload, files),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
      toast.success('Đăng bài thành công')
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}