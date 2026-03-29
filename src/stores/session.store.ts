import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import { tokenStorage } from '@utils/token.storage'

interface SessionState {
  accountId: string | null
  userId: string | null
  role: string | null
  isAuthenticated: boolean

  setSession: (payload: {
    accountId: string
    userId: string
    role: string
    accessToken: string
    refreshToken: string
  }) => void

  clearSession: () => void
}

export const useSessionStore = create<SessionState>()(
  immer((set) => ({
    accountId: tokenStorage.getAccountId(),
    userId: tokenStorage.getUserId(),
    role: null,
    isAuthenticated: tokenStorage.isAuthenticated(),

    setSession: ({ accountId, userId, role, accessToken, refreshToken }) => {
      tokenStorage.setTokens(accessToken, refreshToken)
      tokenStorage.setAccountId(accountId)
      tokenStorage.setUserId(userId)

      set((state) => {
        state.accountId = accountId
        state.userId = userId
        state.role = role
        state.isAuthenticated = true
      })
    },

    clearSession: () => {
      tokenStorage.clear()
      set((state) => {
        state.accountId = null
        state.userId = null
        state.role = null
        state.isAuthenticated = false
      })
    },
  })),
)