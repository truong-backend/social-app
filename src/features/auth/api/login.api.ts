import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { LoginRequest, AuthResponse } from '../types/auth.types'

export const loginApi = async (payload: LoginRequest): Promise<AuthResponse> => {
  const response = await axiosInstance.post('/api/auth/login', payload)
  return unwrapData(response)
}