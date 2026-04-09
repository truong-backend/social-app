import { useMutation, useQueryClient } from '@tanstack/react-query'
import { likePostApi, unlikePostApi } from '../api/like.api'
import { POST_QUERY_KEYS, FEED_PAGE_SIZE } from '@features/post/constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'
import type { Post } from '@features/post/types/post.types'
import type { ApiResponse } from '@/types/api.types'

export const useLikePost = () => {
  const queryClient = useQueryClient()

  // Optimistic update cho cả feed (infinite query) và detail query
  const optimisticUpdate = (postId: string, isLiked: boolean) => {
    // 1. Update detail query
    const detailKey = POST_QUERY_KEYS.detail(postId)
    const prevDetail = queryClient.getQueryData<ApiResponse<Post>>(detailKey)
    queryClient.setQueryData<ApiResponse<Post>>(detailKey, (old) => {
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

    // 2. Update feed infinite query
    const feedKey = POST_QUERY_KEYS.feed()
    const prevFeed = queryClient.getQueryData(feedKey)
    queryClient.setQueryData<{ pages: Post[][]; pageParams: number[] }>(feedKey, (old) => {
      if (!old) return old
      return {
        ...old,
        pages: old.pages.map((page) =>
          page.map((p) =>
            p.id === postId
              ? { ...p, isLiked: !isLiked, likeCount: p.likeCount + (isLiked ? -1 : 1) }
              : p,
          ),
        ),
      }
    })

    return { prevDetail, prevFeed }
  }

  const like = useMutation({
    mutationFn: (postId: string) => likePostApi(postId),
    onMutate: async (postId) => {
      // Lấy trạng thái hiện tại từ feed hoặc detail
      const feedData = queryClient.getQueryData<{ pages: Post[][]; pageParams: number[] }>(POST_QUERY_KEYS.feed())
      const postFromFeed = feedData?.pages.flat().find((p) => p.id === postId)
      const detailData = queryClient.getQueryData<ApiResponse<Post>>(POST_QUERY_KEYS.detail(postId))
      const currentlyLiked = postFromFeed?.isLiked ?? detailData?.data?.isLiked ?? false

      // Nếu đã like rồi thì không làm gì (chặn double-like)
      if (currentlyLiked) return { skip: true }

      await queryClient.cancelQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      await queryClient.cancelQueries({ queryKey: POST_QUERY_KEYS.feed() })
      return optimisticUpdate(postId, false)
    },
    onError: (error, postId, context: any) => {
      if (context?.skip) return
      if (context?.prevDetail) queryClient.setQueryData(POST_QUERY_KEYS.detail(postId), context.prevDetail)
      if (context?.prevFeed) queryClient.setQueryData(POST_QUERY_KEYS.feed(), context.prevFeed)
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_, __, postId, context: any) => {
      if (context?.skip) return
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
    },
  })

  const unlike = useMutation({
    mutationFn: (postId: string) => unlikePostApi(postId),
    onMutate: async (postId) => {
      const feedData = queryClient.getQueryData<{ pages: Post[][]; pageParams: number[] }>(POST_QUERY_KEYS.feed())
      const postFromFeed = feedData?.pages.flat().find((p) => p.id === postId)
      const detailData = queryClient.getQueryData<ApiResponse<Post>>(POST_QUERY_KEYS.detail(postId))
      const currentlyLiked = postFromFeed?.isLiked ?? detailData?.data?.isLiked ?? true

      // Nếu chưa like thì không unlike
      if (!currentlyLiked) return { skip: true }

      await queryClient.cancelQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      await queryClient.cancelQueries({ queryKey: POST_QUERY_KEYS.feed() })
      return optimisticUpdate(postId, true)
    },
    onError: (error, postId, context: any) => {
      if (context?.skip) return
      if (context?.prevDetail) queryClient.setQueryData(POST_QUERY_KEYS.detail(postId), context.prevDetail)
      if (context?.prevFeed) queryClient.setQueryData(POST_QUERY_KEYS.feed(), context.prevFeed)
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_, __, postId, context: any) => {
      if (context?.skip) return
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(postId) })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
    },
  })

  const toggle = (postId: string, isLiked: boolean) => {
    if (like.isPending || unlike.isPending) return
    if (isLiked) unlike.mutate(postId)
    else like.mutate(postId)
  }

  return { toggle, isPending: like.isPending || unlike.isPending }
}
