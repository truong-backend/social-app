import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type {
  UserProfile,
  UserSummary,
  ChangeNameRequest,
  ChangeUsernameRequest,
  ChangeBirthdateRequest,
  ChangeBioRequest,
} from '../types/user.types'

export const getProfileApi = async (userId: string): Promise<UserProfile> => {
  const response = await axiosInstance.get(`/api/users/${userId}`)
  return unwrapData(response)
}

export const getMyProfileApi = async (): Promise<UserProfile> => {
  const response = await axiosInstance.get('/api/users/me')
  return unwrapData(response)
}

export const searchUsersApi = async (keyword: string): Promise<UserSummary[]> => {
  const response = await axiosInstance.get('/api/users/search', { params: { q: keyword } })
  return unwrapData(response) ?? []
}

export const changeNameApi = async (payload: ChangeNameRequest): Promise<void> => {
  await axiosInstance.patch('/api/users/me/name', payload)
}

export const changeUsernameApi = async (payload: ChangeUsernameRequest): Promise<void> => {
  await axiosInstance.patch('/api/users/me/username', payload)
}

export const changeBirthdateApi = async (payload: ChangeBirthdateRequest): Promise<void> => {
  await axiosInstance.patch('/api/users/me/birthdate', payload)
}

export const changeBioApi = async (payload: ChangeBioRequest): Promise<void> => {
  await axiosInstance.patch('/api/users/me/bio', payload)
}

export const updateProfilePictureApi = async (file: File): Promise<void> => {
  const formData = new FormData()
  formData.append('file', file)
  await axiosInstance.patch('/api/users/me/profile-picture', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}