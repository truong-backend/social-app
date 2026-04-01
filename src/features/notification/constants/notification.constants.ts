import type { Notification, NotificationAction } from '../types/notification.types'

export const NOTIFICATION_QUERY_KEYS = {
  all: ['notifications'] as const,
  list: (skip: number) => [...NOTIFICATION_QUERY_KEYS.all, 'list', skip] as const,
} as const

export const NOTIFICATION_ACTION_LABELS: Record<NotificationAction, string> = {
  POST: 'đã đăng bài viết mới',
  BE_FRIEND: 'đã chấp nhận lời mời kết bạn của bạn',
  SENT_ADD_FRIEND_REQUEST: 'đã gửi lời mời kết bạn',
  LIKED_POST: 'đã thích bài viết của bạn',
  COMMENTED_POST: 'đã bình luận bài viết của bạn',
  LIKED_COMMENT: 'đã thích bình luận của bạn',
  REPLIED_COMMENT: 'đã trả lời bình luận của bạn',
}

export const NOTIFICATION_PAGE_SIZE = 20

interface NotificationState {
  notifications: Notification[]
  addNotification: (notification: Notification) => void
  removeNotification: (notificationId: string) => void
  clearNotifications: () => void
  updateNotification: (updated: Notification) => void
}

import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'

export const useNotificationStore = create<NotificationState>()(
  immer((set) => ({
    notifications: [],

    addNotification: (notification: Notification) =>
      set((state) => {
        state.notifications.unshift(notification)
      }),

    removeNotification: (notificationId: string) =>
      set((state) => {
        state.notifications = state.notifications.filter(
          (n: Notification) => n.id !== notificationId,
        )
      }),

    clearNotifications: () =>
      set((state) => {
        state.notifications = []
      }),

    updateNotification: (updated: Notification) =>
      set((state) => {
        const index = state.notifications.findIndex((n: Notification) => n.id === updated.id)
        if (index !== -1) state.notifications[index] = updated
      }),
  })),
)