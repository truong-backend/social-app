import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { registerApi } from '../api/register.api'
import { logoutApi } from '../api/auth.api'
import { useSessionStore } from '@stores/session.store'
import { extractErrorMessage } from '@utils/api-response'
import { websocketService } from '@services/websocket.service'
import type { RegisterRequest } from '../types/auth.types'

export const useRegister = () => {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (payload: RegisterRequest) => registerApi(payload),

    onSuccess: (data) => {
      toast.success(data.message)
      navigate('/confirm-email', { state: { accountId: data.accountId } })
    },

    onError: (error) => {
      toast.error(extractErrorMessage(error))
    },
  })
}

export const useLogout = () => {
  const navigate = useNavigate()
  const clearSession = useSessionStore((state) => state.clearSession)

  return useMutation({
    mutationFn: logoutApi,

    onSuccess: () => {
      clearSession()
      websocketService.disconnect()
      navigate('/login')
    },

    onError: () => {
      // Force logout even if API fails
      clearSession()
      websocketService.disconnect()
      navigate('/login')
    },
  })
}