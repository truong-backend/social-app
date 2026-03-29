import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { Post } from '../types/post.types'

export const getPostApi = async (postId: string): Promise<Post> => {
  const response = await axiosInstance.get(`/api/posts/${postId}`)
  return unwrapData(response)
}