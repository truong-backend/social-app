import { useState } from 'react'
import { useNotificationStore } from '../store/notification.store'
import { NotificationDropdown } from './NotificationDropdown'

export const NotificationBell = () => {
  const [isOpen, setIsOpen] = useState(false)
  const unreadCount = useNotificationStore((state) => state.unreadCount)

  return (
    <div className="notification-bell">
      <button
        className="notification-bell__btn"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-label={`Thông báo${unreadCount > 0 ? ` (${unreadCount} chưa đọc)` : ''}`}
      >
        🔔
        {unreadCount > 0 && (
          <span className="notification-bell__badge">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <NotificationDropdown onClose={() => setIsOpen(false)} />
      )}
    </div>
  )
}