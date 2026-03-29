import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { RegisterRequest, RegisterResponse } from '../types/auth.types'

export const registerApi = async (payload: RegisterRequest): Promise<RegisterResponse> => {
  const response = await axiosInstance.post('/api/auth/register', payload)
  return unwrapData(response)
}