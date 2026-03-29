import { axiosInstance } from '@services/axios.instance'

export const likePostApi = async (postId: string): Promise<void> => {
  await axiosInstance.post(`/api/posts/${postId}/like`)
}

export const unlikePostApi = async (postId: string): Promise<void> => {
  await axiosInstance.delete(`/api/posts/${postId}/like`)
}

export const likeCommentApi = async (commentId: string): Promise<void> => {
  await axiosInstance.post(`/api/comments/${commentId}/like`)
}

export const unlikeCommentApi = async (commentId: string): Promise<void> => {
  await axiosInstance.delete(`/api/comments/${commentId}/like`)
}