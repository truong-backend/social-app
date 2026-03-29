import { useInfiniteQuery } from '@tanstack/react-query'
import { getCommentsApi } from '../api/comment.api'
import { COMMENT_QUERY_KEYS, COMMENT_PAGE_SIZE } from '../constants/comment.constants'

export const useComments = (postId: string) => {
  return useInfiniteQuery({
    queryKey: COMMENT_QUERY_KEYS.byPost(postId),
    queryFn: ({ pageParam }) =>
      getCommentsApi(postId, pageParam as number, COMMENT_PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < COMMENT_PAGE_SIZE) return undefined
      return allPages.length * COMMENT_PAGE_SIZE
    },
    enabled: !!postId,
  })
}