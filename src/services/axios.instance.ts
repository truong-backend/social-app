import axios from 'axios'
import axiosRetry from 'axios-retry'
import { ENV } from '@config/environment'
import { tokenStorage } from '@utils/token.storage'

export const axiosInstance = axios.create({
  baseURL: ENV.API_BASE_URL,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
})

axiosRetry(axiosInstance, {
  retries: 1,
  retryCondition: (error) => error.response?.status === 503,
})

// ── Request interceptor — attach token ────────────────────────
axiosInstance.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Response interceptor — handle 401 + token refresh ─────────
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((promise) => {
    if (error) promise.reject(error)
    else promise.resolve(token!)
  })
  failedQueue = []
}

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return axiosInstance(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = tokenStorage.getRefreshToken()
        if (!refreshToken) throw new Error('No refresh token')

        const { data } = await axios.post(`${ENV.API_BASE_URL}/api/auth/refresh`, {
          refreshToken,
        })

        tokenStorage.setTokens(data.data.accessToken, data.data.refreshToken)
        processQueue(null, data.data.accessToken)
        originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`
        return axiosInstance(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        tokenStorage.clear()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)