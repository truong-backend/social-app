import { useInfiniteQuery } from '@tanstack/react-query'
import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import { POST_QUERY_KEYS, FEED_PAGE_SIZE } from '../constants/post.constants'
import type { Post } from '../types/post.types'

const getPostsByAuthorApi = async (
  authorId: string,
  skip:     number,
  limit:    number,
): Promise<Post[]> => {
  const response = await axiosInstance.get(`/api/posts/author/${authorId}`, {
    params: { skip, limit },
  })
  return unwrapData(response) ?? []
}

export const usePostsByAuthor = (authorId: string, _viewerId?: string) => {
  return useInfiniteQuery({
    queryKey: POST_QUERY_KEYS.byAuthor(authorId),
    queryFn: ({ pageParam }) =>
      getPostsByAuthorApi(authorId, pageParam as number, FEED_PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < FEED_PAGE_SIZE) return undefined
      return allPages.length * FEED_PAGE_SIZE
    },
    enabled: !!authorId,
  })
}