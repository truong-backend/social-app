import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { Comment, CreateCommentRequest, UpdateCommentRequest } from '../types/comment.types'

export const getCommentsApi = async (
  postId: string,
  skip = 0,
  limit = 10,
): Promise<Comment[]> => {
  const response = await axiosInstance.get(`/api/posts/${postId}/comments`, {
    params: { skip, limit },
  })
  return unwrapData(response) ?? []
}

export const createCommentApi = async (
  postId: string,
  payload: CreateCommentRequest,
  files?: File[],
): Promise<Comment> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.post(`/api/posts/${postId}/comments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapData(response)
}

export const replyCommentApi = async (
  postId: string,
  commentId: string,
  payload: CreateCommentRequest,
  files?: File[],
): Promise<Comment> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.post(
    `/api/posts/${postId}/comments/${commentId}/replies`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
  return unwrapData(response)
}

export const updateCommentApi = async (
  commentId: string,
  payload:   UpdateCommentRequest,
  files?:    File[],
): Promise<Comment> => {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files?.forEach((file) => formData.append('files', file))

  const response = await axiosInstance.put(`/api/comments/${commentId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapData(response)
}

export const deleteCommentApi = async (commentId: string): Promise<void> => {
  await axiosInstance.delete(`/api/comments/${commentId}`)
}

export const getRepliesApi = async (
  commentId: string,
  skip = 0,
  limit = 10,
): Promise<Comment[]> => {
  const response = await axiosInstance.get(
    `/api/posts/comments/${commentId}/replies`,
    { params: { skip, limit } },
  )
  return unwrapData(response) ?? []
}