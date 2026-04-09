import { useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useNotificationStore } from '../store/notification.store'
import { useNotifications } from '../hooks/useNotifications'
import { NOTIFICATION_ACTION_LABELS } from '../constants/notification.constants'
import { formatRelativeTime } from '@utils/date.formatter'

const notifIcon = (action: string) => {
  switch (action) {
    case 'LIKED_POST':
    case 'LIKED_COMMENT':    return { icon: 'favorite', color: 'text-red-500 bg-red-50' }
    case 'COMMENTED_POST':   return { icon: 'chat_bubble', color: 'text-blue-500 bg-blue-50' }
    case 'REPLIED_COMMENT':  return { icon: 'reply', color: 'text-indigo-500 bg-indigo-50' }
    case 'SENT_ADD_FRIEND_REQUEST': return { icon: 'person_add', color: 'text-emerald-500 bg-emerald-50' }
    case 'BE_FRIEND':        return { icon: 'people', color: 'text-green-500 bg-green-50' }
    case 'POST':             return { icon: 'article', color: 'text-violet-500 bg-violet-50' }
    default:                 return { icon: 'notifications', color: 'text-primary bg-primary/10' }
  }
}

interface NotificationDropdownProps {
  onClose: () => void
}

export const NotificationDropdown = ({ onClose }: NotificationDropdownProps) => {
  const dropdownRef = useRef<HTMLDivElement>(null)
  const { isLoading } = useNotifications()
  const { notifications, markAllAsRead, markAsRead } = useNotificationStore()

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        onClose()
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [onClose])

  return (
    <div
      ref={dropdownRef}
      className="absolute right-0 top-full mt-2 w-96 bg-surface-container-lowest rounded-2xl shadow-[0_20px_60px_rgba(35,44,81,0.15)] border border-outline-variant/10 z-50 overflow-hidden"
    >
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4 border-b border-outline-variant/10">
        <h3
          className="font-bold text-on-surface text-lg"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Thông báo
        </h3>
        {notifications.some((n) => !n.isRead) && (
          <button
            className="flex items-center gap-1.5 text-primary font-semibold text-sm hover:bg-primary/5 px-3 py-1.5 rounded-full transition-colors"
            onClick={markAllAsRead}
          >
            <span className="material-symbols-outlined text-lg">done_all</span>
            Đánh dấu đã đọc
          </button>
        )}
      </div>

      {/* List */}
      <div className="max-h-96 overflow-y-auto" style={{ scrollbarWidth: 'thin' }}>
        {isLoading ? (
          <div className="flex items-center justify-center py-12 text-sm text-on-surface-variant gap-2">
            <span className="w-4 h-4 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
            Đang tải...
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3 text-on-surface-variant">
            <span className="material-symbols-outlined text-4xl opacity-40">notifications_off</span>
            <p className="text-sm">Không có thông báo nào</p>
          </div>
        ) : (
          notifications.slice(0, 8).map((notification) => {
            const { icon, color } = notifIcon(notification.action)
            return (
              <div
                key={notification.id}
                className={`relative flex items-start gap-4 px-5 py-4 cursor-pointer transition-all ${
                  !notification.isRead
                    ? 'bg-primary-container/10 hover:bg-primary-container/20'
                    : 'hover:bg-surface-container-low'
                }`}
                onClick={() => markAsRead(notification.id)}
                role="button"
                tabIndex={0}
              >
                {!notification.isRead && (
                  <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary rounded-r" />
                )}

                <div className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${color}`}>
                  <span
                    className="material-symbols-outlined text-xl"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    {icon}
                  </span>
                </div>

                <div className="flex-1 min-w-0">
                  <p className="text-sm text-on-surface leading-snug">
                    {NOTIFICATION_ACTION_LABELS[notification.action]}
                  </p>
                  <span className={`text-xs font-medium mt-1 block ${
                    !notification.isRead ? 'text-primary' : 'text-on-surface-variant'
                  }`}>
                    {formatRelativeTime(notification.sentAt)}
                  </span>
                </div>

                {!notification.isRead && (
                  <div className="w-2.5 h-2.5 bg-primary rounded-full flex-shrink-0 mt-1" />
                )}
              </div>
            )
          })
        )}
      </div>

      {/* Footer - link thật đến /notifications */}
      <div className="px-5 py-3 border-t border-outline-variant/10 text-center">
        <Link
          to="/notifications"
          onClick={onClose}
          className="text-sm font-semibold text-primary hover:bg-primary/5 px-4 py-1.5 rounded-full transition-colors inline-block"
        >
          Xem tất cả thông báo
        </Link>
      </div>
    </div>
  )
}
