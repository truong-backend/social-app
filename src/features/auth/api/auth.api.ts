import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type {
  ConfirmEmailRequest,
  AuthResponse,
  PrepareResetPasswordRequest,
  ConfirmResetCodeRequest,
  UpdatePasswordRequest,
} from '../types/auth.types'

export const logoutApi = async (): Promise<void> => {
  const token = localStorage.getItem('access_token') ?? ''
  await axiosInstance.post('/api/auth/logout', null, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

export const confirmEmailApi = async (payload: ConfirmEmailRequest): Promise<AuthResponse> => {
  const response = await axiosInstance.post('/api/auth/confirm-email', payload)
  return unwrapData(response)
}

export const prepareResetPasswordApi = async (
  payload: PrepareResetPasswordRequest,
): Promise<void> => {
  await axiosInstance.post('/api/auth/prepare-reset-password', payload)
}

export const confirmResetCodeApi = async (payload: ConfirmResetCodeRequest): Promise<void> => {
  await axiosInstance.post('/api/auth/confirm-reset-code', payload)
}

export const updatePasswordApi = async (payload: UpdatePasswordRequest): Promise<void> => {
  await axiosInstance.put('/api/auth/update-password', payload)
}