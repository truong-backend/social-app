import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Notification } from '../types/notification.types'

interface NotificationState {
  notifications: Notification[]
  unreadCount: number

  addNotification: (notification: Notification) => void
  markAsRead: (notificationId: string) => void
  markAllAsRead: () => void
  setNotifications: (notifications: Notification[]) => void
}

export const useNotificationStore = create<NotificationState>()(
  immer((set) => ({
    notifications: [],
    unreadCount: 0,

    addNotification: (notification) =>
      set((state) => {
        state.notifications.unshift(notification)
        if (!notification.isRead) state.unreadCount += 1
      }),

    markAsRead: (notificationId) =>
      set((state) => {
        const notification = state.notifications.find((n: Notification) => n.id === notificationId)
        if (notification && !notification.isRead) {
          notification.isRead = true
          state.unreadCount = Math.max(0, state.unreadCount - 1)
        }
      }),

    markAllAsRead: () =>
      set((state) => {
        state.notifications.forEach((n: Notification) => { n.isRead = true })
        state.unreadCount = 0
      }),

    setNotifications: (notifications) =>
      set((state) => {
        state.notifications = notifications
        state.unreadCount = notifications.filter((n: Notification) => !n.isRead).length
      }),
  })),
)