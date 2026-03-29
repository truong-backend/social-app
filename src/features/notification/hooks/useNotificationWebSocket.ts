import { useEffect } from 'react'
import { websocketService } from '@services/websocket.service'
import { useNotificationStore } from '../store/notification.store'
import { useSessionStore } from '@stores/session.store'
import type { Notification } from '../types/notification.types'
import toast from 'react-hot-toast'
import { NOTIFICATION_ACTION_LABELS } from '../constants/notification.constants'

export const useNotificationWebSocket = () => {
  const userId = useSessionStore((state) => state.userId)
  const addNotification = useNotificationStore((state) => state.addNotification)

  useEffect(() => {
    if (!userId) return

    const topic = `/user/${userId}/queue/notification`

    websocketService.subscribe(topic, (frame) => {
      const notification: Notification = JSON.parse(frame.body)
      addNotification(notification)

      const label = NOTIFICATION_ACTION_LABELS[notification.action]
      toast(`Ai đó ${label}`, { icon: '🔔', duration: 4000 })
    })

    return () => {
      websocketService.unsubscribe(topic)
    }
  }, [userId, addNotification])
}