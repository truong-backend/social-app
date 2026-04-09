import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { likeCommentApi, unlikeCommentApi } from '@features/like/api/like.api'
import { COMMENT_QUERY_KEYS } from '@features/comment/constants/comment.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { Comment } from '@features/comment/types/comment.types'

export const useLikeComment = (postId: string) => {
  const queryClient = useQueryClient()

  // Helper: lấy trạng thái isLiked hiện tại của comment từ cache
  const getCurrentIsLiked = (commentId: string): boolean => {
    const data = queryClient.getQueryData<{ pages: Comment[][] }>(
      COMMENT_QUERY_KEYS.byPost(postId),
    )
    return data?.pages.flat().find((c) => c.id === commentId)?.isLiked ?? false
  }

  const optimisticToggle = (commentId: string, isLiked: boolean) => {
    const queryKey = COMMENT_QUERY_KEYS.byPost(postId)
    const snapshot = queryClient.getQueryData(queryKey)

    queryClient.setQueriesData<{ pages: Comment[][] }>({ queryKey }, (old) => {
      if (!old) return old
      return {
        ...old,
        pages: old.pages.map((page) =>
          page.map((comment) =>
            comment.id === commentId
              ? {
                  ...comment,
                  isLiked: !isLiked,
                  likeCount: comment.likeCount + (isLiked ? -1 : 1),
                }
              : comment,
          ),
        ),
      }
    })
    return { snapshot }
  }

  const like = useMutation({
    mutationFn: (commentId: string) => likeCommentApi(commentId),
    onMutate: async (commentId) => {
      // ✅ FIX: Guard chặn double-like — giống useLikePost
      const currentlyLiked = getCurrentIsLiked(commentId)
      if (currentlyLiked) return { skip: true }

      await queryClient.cancelQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
      return optimisticToggle(commentId, false)
    },
    onError: (error, _commentId, context: any) => {
      if (context?.skip) return
      if (context?.snapshot) {
        queryClient.setQueryData(COMMENT_QUERY_KEYS.byPost(postId), context.snapshot)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_,__,___, context: any) => {
      if (context?.skip) return
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
    },
  })

  const unlike = useMutation({
    mutationFn: (commentId: string) => unlikeCommentApi(commentId),
    onMutate: async (commentId) => {
      // ✅ FIX: Guard chặn unlike khi chưa like
      const currentlyLiked = getCurrentIsLiked(commentId)
      if (!currentlyLiked) return { skip: true }

      await queryClient.cancelQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
      return optimisticToggle(commentId, true)
    },
    onError: (error, _commentId, context: any) => {
      if (context?.skip) return
      if (context?.snapshot) {
        queryClient.setQueryData(COMMENT_QUERY_KEYS.byPost(postId), context.snapshot)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: (_,__,___, context: any) => {
      if (context?.skip) return
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
    },
  })

  const toggle = (commentId: string, isLiked: boolean) => {
    if (like.isPending || unlike.isPending) return
    if (isLiked) unlike.mutate(commentId)
    else like.mutate(commentId)
  }

  return { toggle, isPending: like.isPending || unlike.isPending }
}
