import { useQuery } from '@tanstack/react-query'
import { getPostApi } from '../api/getPost.api'
import { POST_QUERY_KEYS } from '../constants/post.constants'

export const usePost = (postId: string) => {
  return useQuery({
    queryKey: POST_QUERY_KEYS.detail(postId),
    queryFn: () => getPostApi(postId),
    enabled: !!postId,
  })
}