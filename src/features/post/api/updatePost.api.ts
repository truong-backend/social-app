import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { UpdatePostContentRequest, UpdatePostPrivacyRequest, Post } from '../types/post.types'

export const updatePostContentApi = async (
  postId: string,
  payload: UpdatePostContentRequest,
  files?: File[],
): Promise<Post> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.put(`/api/posts/${postId}/content`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapData(response)
}

export const updatePostPrivacyApi = async (
  postId: string,
  payload: UpdatePostPrivacyRequest,
): Promise<void> => {
  await axiosInstance.patch(`/api/posts/${postId}/privacy`, payload)
}