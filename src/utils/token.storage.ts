import { ENV } from '@config/environment'

export const tokenStorage = {
  getAccessToken: (): string | null =>
    localStorage.getItem(ENV.ACCESS_TOKEN_KEY),

  getRefreshToken: (): string | null =>
    localStorage.getItem(ENV.REFRESH_TOKEN_KEY),

  setTokens: (accessToken: string, refreshToken: string): void => {
    localStorage.setItem(ENV.ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(ENV.REFRESH_TOKEN_KEY, refreshToken)
  },

  setUserId: (userId: string): void =>
    localStorage.setItem(ENV.USER_ID_KEY, userId),

  getUserId: (): string | null =>
    localStorage.getItem(ENV.USER_ID_KEY),

  setAccountId: (accountId: string): void =>
    localStorage.setItem(ENV.ACCOUNT_ID_KEY, accountId),

  getAccountId: (): string | null =>
    localStorage.getItem(ENV.ACCOUNT_ID_KEY),

  clear: (): void => {
    localStorage.removeItem(ENV.ACCESS_TOKEN_KEY)
    localStorage.removeItem(ENV.REFRESH_TOKEN_KEY)
    localStorage.removeItem(ENV.USER_ID_KEY)
    localStorage.removeItem(ENV.ACCOUNT_ID_KEY)
  },

  isAuthenticated: (): boolean =>
    !!localStorage.getItem(ENV.ACCESS_TOKEN_KEY),
}