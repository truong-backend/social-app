import { useState } from 'react'
import { useNotificationStore } from '../store/notification.store'
import { NotificationDropdown } from './NotificationDropdown'

export const NotificationBell = () => {
  const [isOpen, setIsOpen] = useState(false)
  const unreadCount = useNotificationStore((state) => state.unreadCount)

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(p => !p)}
        className="relative p-2 text-on-surface-variant hover:bg-slate-100/50 rounded-full transition-colors cursor-pointer"
        aria-label={`Thông báo${unreadCount > 0 ? ` (${unreadCount})` : ''}`}
      >
        <span className="material-symbols-outlined">notifications</span>
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 min-w-[18px] h-[18px] bg-primary text-on-primary text-[10px] font-bold rounded-full flex items-center justify-center px-1">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && <NotificationDropdown onClose={() => setIsOpen(false)} />}
    </div>
  )
}
