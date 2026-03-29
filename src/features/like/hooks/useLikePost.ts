import { useMutation, useQueryClient } from '@tanstack/react-query'
import { likePostApi, unlikePostApi } from '../api/like.api'
import { POST_QUERY_KEYS } from '@features/post/constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'
import type { Post } from '@features/post/types/post.types'
import type { ApiResponse } from '@/types/api.types'

export const useLikePost = () => {
  const queryClient = useQueryClient()

  const optimisticToggle = (postId: string, isLiked: boolean) => {
    const queryKey = POST_QUERY_KEYS.detail(postId)

    queryClient.setQueryData<ApiResponse<Post>>(queryKey, (old) => {
      if (!old?.data) return old
      return {
        ...old,
        data: {
          ...old.data,
          isLiked: !isLiked,
          likeCount: old.data.likeCount + (isLiked ? -1 : 1),
        },
      }
    })

    return { previousData: queryClient.getQueryData<ApiResponse<Post>>(queryKey) }
  }

  const like = useMutation({
    mutationFn: (postId: string) => likePostApi(postId),
    onMutate: (postId) => {
      const post = queryClient.getQueryData<ApiResponse<Post>>(POST_QUERY_KEYS.detail(postId))
      return optimisticToggle(postId, post?.data?.isLiked ?? false)
    },
    onError: (error, postId, context) => {
      if (context?.previousData) {
        queryClient.setQueryData(POST_QUERY_KEYS.detail(postId), context.previousData)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_, __, postId) => {
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
    },
  })

  const unlike = useMutation({
    mutationFn: (postId: string) => unlikePostApi(postId),
    onMutate: (postId) => {
      const post = queryClient.getQueryData<ApiResponse<Post>>(POST_QUERY_KEYS.detail(postId))
      return optimisticToggle(postId, post?.data?.isLiked ?? true)
    },
    onError: (error, postId, context) => {
      if (context?.previousData) {
        queryClient.setQueryData(POST_QUERY_KEYS.detail(postId), context.previousData)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_, __, postId) => {
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
    },
  })

  const toggle = (postId: string, isLiked: boolean) => {
    if (isLiked) unlike.mutate(postId)
    else like.mutate(postId)
  }

  return { toggle, isPending: like.isPending || unlike.isPending }
}