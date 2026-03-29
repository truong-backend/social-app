import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { loginApi } from '../api/login.api'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import type { LoginRequest } from '../types/auth.types'

export const useLogin = () => {
  const navigate = useNavigate()
  const setSession = useSessionStore((state) => state.setSession)

  return useMutation({
    mutationFn: (payload: LoginRequest) => loginApi(payload),

    onSuccess: (data) => {
      setSession({
        accountId: data.accountId,
        userId: data.userId,
        role: data.role,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      })
      navigate('/feed')
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}