import { useQuery } from '@tanstack/react-query'
import { useEffect } from 'react'
import { getNotificationsApi } from '../api/notification.api'
import { useNotificationStore } from '../store/notification.store'
import { NOTIFICATION_QUERY_KEYS, NOTIFICATION_PAGE_SIZE } from '../constants/notification.constants'

export const useNotifications = () => {
  const setNotifications = useNotificationStore((state) => state.setNotifications)

  const query = useQuery({
    queryKey: NOTIFICATION_QUERY_KEYS.list(0),
    queryFn: () => getNotificationsApi(0, NOTIFICATION_PAGE_SIZE),
    staleTime: 1000 * 60,
  })

  useEffect(() => {
    if (query.data) {
      setNotifications(query.data)
    }
  }, [query.data, setNotifications])

  return query
}