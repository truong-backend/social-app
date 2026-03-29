import { useInfiniteQuery } from '@tanstack/react-query'
import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import { POST_QUERY_KEYS, FEED_PAGE_SIZE } from '../constants/post.constants'
import type { Post } from '../types/post.types'

const getFeedApi = async (skip: number, limit: number): Promise<Post[]> => {
  const response = await axiosInstance.get('/api/posts/feed', { params: { skip, limit } })
  return unwrapData(response) ?? []
}

export const useInfiniteFeed = () => {
  return useInfiniteQuery({
    queryKey: POST_QUERY_KEYS.feed(),
    queryFn: ({ pageParam }) => getFeedApi(pageParam as number, FEED_PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < FEED_PAGE_SIZE) return undefined
      return allPages.length * FEED_PAGE_SIZE
    },
  })
}