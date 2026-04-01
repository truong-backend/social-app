import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { likeCommentApi, unlikeCommentApi } from '../api/like.api'
import { COMMENT_QUERY_KEYS } from '@features/comment/constants/comment.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { Comment } from '@features/comment/types/comment.types'

export const useLikeComment = (postId: string) => {
  const queryClient = useQueryClient()

  const optimisticToggle = (commentId: string, isLiked: boolean) => {
    const queryKey = COMMENT_QUERY_KEYS.byPost(postId)
    queryClient.setQueriesData<{ pages: Comment[][] }>({ queryKey }, (old) => {
      if (!old) return old
      return {
        ...old,
        pages: old.pages.map((page) =>
          page.map((comment) =>
            comment.id === commentId
              ? {
                  ...comment,
                  isLiked:   !isLiked,
                  likeCount: comment.likeCount + (isLiked ? -1 : 1),
                }
              : comment,
          ),
        ),
      }
    })
    return { snapshot: queryClient.getQueryData(queryKey) }
  }

  const like = useMutation({
    mutationFn: (commentId: string) => likeCommentApi(commentId),
    onMutate:   (commentId) => optimisticToggle(commentId, false),
    onError:    (error, _commentId, context) => {
      if (context?.snapshot) {
        queryClient.setQueryData(COMMENT_QUERY_KEYS.byPost(postId), context.snapshot)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
    },
  })

  const unlike = useMutation({
    mutationFn: (commentId: string) => unlikeCommentApi(commentId),
    onMutate:   (commentId) => optimisticToggle(commentId, true),
    onError:    (error, _commentId, context) => {
      if (context?.snapshot) {
        queryClient.setQueryData(COMMENT_QUERY_KEYS.byPost(postId), context.snapshot)
      }
      toast.error(extractErrorMessage(error))
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: COMMENT_QUERY_KEYS.byPost(postId) })
    },
  })

  const toggle = (commentId: string, isLiked: boolean) => {
    if (isLiked) unlike.mutate(commentId)
    else like.mutate(commentId)
  }

  return { toggle, isPending: like.isPending || unlike.isPending }
}