import { useState, useCallback } from 'react'
import { useQuery, useInfiniteQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useNotificationStore } from '@features/notification/store/notification.store'
import { getNotificationsApi } from '@features/notification/api/notification.api'
import { NOTIFICATION_ACTION_LABELS, NOTIFICATION_QUERY_KEYS } from '@features/notification/constants/notification.constants'
import { formatRelativeTime } from '@utils/date.formatter'
import { Spinner } from '@components/feedback/Spinner'
import type { Notification } from '@features/notification/types/notification.types'

const PAGE_SIZE = 20

const notifIcon = (action: string) => {
  switch (action) {
    case 'LIKED_POST':
    case 'LIKED_COMMENT': return { icon: 'favorite', color: 'text-red-500 bg-red-50' }
    case 'COMMENTED_POST': return { icon: 'chat_bubble', color: 'text-blue-500 bg-blue-50' }
    case 'REPLIED_COMMENT': return { icon: 'reply', color: 'text-indigo-500 bg-indigo-50' }
    case 'SENT_ADD_FRIEND_REQUEST': return { icon: 'person_add', color: 'text-emerald-500 bg-emerald-50' }
    case 'BE_FRIEND': return { icon: 'people', color: 'text-green-500 bg-green-50' }
    case 'POST': return { icon: 'article', color: 'text-violet-500 bg-violet-50' }
    default: return { icon: 'notifications', color: 'text-primary bg-primary/10' }
  }
}

const targetLink = (notif: Notification): string => {
  switch (notif.targetType) {
    case 'POST':    return `/posts/${notif.targetId}`
    case 'COMMENT': return `/posts/${notif.targetId}`
    case 'FRIEND':
    case 'REQUEST': return `/profile/${notif.byUserId}`
    default:        return '#'
  }
}

export const NotificationsPage = () => {
  const { markAsRead, markAllAsRead } = useNotificationStore()

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useInfiniteQuery({
    queryKey: ['notifications', 'all'],
    queryFn: ({ pageParam }) => getNotificationsApi(pageParam as number, PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < PAGE_SIZE) return undefined
      return allPages.length * PAGE_SIZE
    },
    staleTime: 1000 * 30,
  })

  const allNotifications = data?.pages.flat() ?? []
  const hasUnread = allNotifications.some((n) => !n.isRead)

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 pb-24 md:pb-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1
            className="text-3xl font-extrabold tracking-tight text-on-surface"
            style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
          >
            Thông báo
          </h1>
          <p className="text-sm text-on-surface-variant mt-0.5">
            {allNotifications.filter((n) => !n.isRead).length > 0
              ? `${allNotifications.filter((n) => !n.isRead).length} chưa đọc`
              : 'Tất cả đã đọc'}
          </p>
        </div>
        {hasUnread && (
          <button
            onClick={markAllAsRead}
            className="flex items-center gap-2 text-primary font-semibold text-sm hover:bg-primary/5 px-4 py-2 rounded-full transition-colors border border-primary/20"
          >
            <span className="material-symbols-outlined text-lg">done_all</span>
            Đánh dấu tất cả đã đọc
          </button>
        )}
      </div>

      {/* Content */}
      <div className="bg-surface-container-lowest rounded-2xl shadow-sm border border-outline-variant/10 overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center py-16 gap-2 text-on-surface-variant">
            <Spinner size="md" />
            <span className="text-sm">Đang tải thông báo...</span>
          </div>
        ) : allNotifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-4 text-on-surface-variant">
            <span className="material-symbols-outlined text-5xl opacity-30">notifications_off</span>
            <p className="font-medium">Không có thông báo nào</p>
            <p className="text-sm">Khi có hoạt động mới, thông báo sẽ xuất hiện ở đây</p>
          </div>
        ) : (
          <>
            {allNotifications.map((notification, idx) => {
              const { icon, color } = notifIcon(notification.action)
              const link = targetLink(notification)
              return (
                <Link
                  key={notification.id}
                  to={link}
                  onClick={() => markAsRead(notification.id)}
                  className={`relative flex items-start gap-4 px-5 py-4 transition-all border-b border-outline-variant/5 last:border-b-0 ${
                    !notification.isRead
                      ? 'bg-primary-container/8 hover:bg-primary-container/15'
                      : 'hover:bg-surface-container-low'
                  }`}
                >
                  {/* Unread bar */}
                  {!notification.isRead && (
                    <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary rounded-r" />
                  )}

                  {/* Icon */}
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${color}`}>
                    <span
                      className="material-symbols-outlined text-xl"
                      style={{ fontVariationSettings: "'FILL' 1" }}
                    >
                      {icon}
                    </span>
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-on-surface leading-snug">
                      <span className="font-semibold">
                        {NOTIFICATION_ACTION_LABELS[notification.action] ?? notification.action}
                      </span>
                    </p>
                    <span className={`text-xs font-medium mt-1 block ${
                      !notification.isRead ? 'text-primary' : 'text-on-surface-variant'
                    }`}>
                      {formatRelativeTime(notification.sentAt)}
                    </span>
                  </div>

                  {/* Unread dot */}
                  {!notification.isRead && (
                    <div className="w-2.5 h-2.5 bg-primary rounded-full flex-shrink-0 mt-1" />
                  )}
                </Link>
              )
            })}

            {/* Load more */}
            <div className="px-5 py-4 text-center border-t border-outline-variant/10">
              {hasNextPage ? (
                <button
                  onClick={() => fetchNextPage()}
                  disabled={isFetchingNextPage}
                  className="text-sm font-semibold text-primary hover:bg-primary/5 px-6 py-2 rounded-full transition-colors disabled:opacity-50"
                >
                  {isFetchingNextPage ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
                      Đang tải...
                    </span>
                  ) : (
                    'Xem thêm thông báo'
                  )}
                </button>
              ) : (
                <p className="text-xs text-on-surface-variant">Đã hiển thị tất cả thông báo</p>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
