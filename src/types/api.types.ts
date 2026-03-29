export interface ApiResponse<T = unknown> {
  success: boolean
  message: string | null
  data: T | null
}

export interface PaginationParams {
  skip?: number
  limit?: number
}

export type Privacy = 'PUBLIC' | 'FRIENDS' | 'PRIVATE'

export type Role = 'USER' | 'ADMIN'