import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import type { Notification } from '../types/notification.types'

export const getNotificationsApi = async (
  skip = 0,
  limit = 20,
): Promise<Notification[]> => {
  const response = await axiosInstance.get('/api/notifications', {
    params: { skip, limit },
  })
  return unwrapData(response) ?? []
}