import type { ApiResponse } from '@/types/api.types'
import type { AxiosResponse } from 'axios'

export const unwrapData = <T>(response: AxiosResponse<ApiResponse<T>>): T => {
  return response.data.data as T
}

export const extractErrorMessage = (error: unknown): string => {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error
  ) {
    const axiosError = error as { response?: { data?: { message?: string } } }
    return axiosError.response?.data?.message ?? 'Something went wrong'
  }
  return 'Something went wrong'
}