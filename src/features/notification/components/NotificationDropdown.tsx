import { useEffect, useRef } from 'react'
import { useNotificationStore } from '../store/notification.store'
import { useNotifications } from '../hooks/useNotifications'
import { NOTIFICATION_ACTION_LABELS } from '../constants/notification.constants'
import { formatRelativeTime } from '@utils/date.formatter'

interface NotificationDropdownProps {
  onClose: () => void
}

export const NotificationDropdown = ({ onClose }: NotificationDropdownProps) => {
  const dropdownRef = useRef<HTMLDivElement>(null)
  const { isLoading } = useNotifications()
  const { notifications, markAllAsRead, markAsRead } = useNotificationStore()

  // Close on outside click
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
    <div ref={dropdownRef} className="notification-dropdown">
      <div className="notification-dropdown__header">
        <h3 className="notification-dropdown__title">Thông báo</h3>
        {notifications.some((n) => !n.isRead) && (
          <button
            className="notification-dropdown__mark-all-btn"
            onClick={markAllAsRead}
          >
            Đánh dấu tất cả đã đọc
          </button>
        )}
      </div>

      <div className="notification-dropdown__list">
        {isLoading ? (
          <div className="notification-dropdown__loading">Đang tải...</div>
        ) : notifications.length === 0 ? (
          <div className="notification-dropdown__empty">Không có thông báo nào</div>
        ) : (
          notifications.map((notification) => (
            <div
              key={notification.id}
              className={`notification-dropdown__item ${
                !notification.isRead ? 'notification-dropdown__item--unread' : ''
              }`}
              onClick={() => markAsRead(notification.id)}
              role="button"
              tabIndex={0}
            >
              <p className="notification-dropdown__item-text">
                {NOTIFICATION_ACTION_LABELS[notification.action]}
              </p>
              <span className="notification-dropdown__item-time">
                {formatRelativeTime(notification.sentAt)}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  )
}