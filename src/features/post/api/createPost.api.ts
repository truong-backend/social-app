import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { CreatePostRequest, Post } from '../types/post.types'

export const createPostApi = async (
  payload: CreatePostRequest,
  files?: File[],
): Promise<Post> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.post('/api/posts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapData(response)
}