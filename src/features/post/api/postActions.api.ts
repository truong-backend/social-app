import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { SharePostRequest, Post } from '../types/post.types'

export const deletePostApi = async (postId: string): Promise<void> => {
  await axiosInstance.delete(`/api/posts/${postId}`)
}

export const sharePostApi = async (
  originalPostId: string,
  payload: SharePostRequest,
): Promise<Post> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))

  const response = await axiosInstance.post(`/api/posts/${originalPostId}/share`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapData(response)
}

// FIX: đổi từ /api/users/search sang /api/posts/search
export const searchPostsApi = async (keyword: string): Promise<Post[]> => {
  const response = await axiosInstance.get('/api/posts/search', {
    params: { q: keyword },
  })
  return unwrapData(response) ?? []
}
