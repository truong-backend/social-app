export const ENV = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  WS_URL: import.meta.env.VITE_WS_URL ?? 'http://localhost:8080/ws',
  ACCESS_TOKEN_KEY: 'access_token',
  REFRESH_TOKEN_KEY: 'refresh_token',
  USER_ID_KEY: 'user_id',
  ACCOUNT_ID_KEY: 'account_id',
} as const