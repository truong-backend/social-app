import type { Message } from '../types/chat.types'
import { formatRelativeTime } from '@utils/date.formatter'

interface MessageBubbleProps {
  message:      Message
  isOwnMessage: boolean
  onDelete?:    (messageId: string) => void
  onEdit?:      (message: Message) => void
}

export const MessageBubble = ({
  message,
  isOwnMessage,
  onDelete,
  onEdit,
}: MessageBubbleProps) => {
  return (
    <div
      className={`group flex items-end gap-3 max-w-[80%] ${
        isOwnMessage ? 'flex-row-reverse ml-auto' : 'flex-row'
      }`}
    >
      {/* Bubble */}
      <div className={`flex flex-col gap-1 ${isOwnMessage ? 'items-end' : 'items-start'}`}>
        <div
          className={`px-4 py-3 rounded-2xl shadow-sm ${
            isOwnMessage
              ? 'bg-gradient-to-br from-primary to-primary-container text-white rounded-br-none shadow-md'
              : 'bg-surface-container-high/50 text-on-surface rounded-bl-none'
          }`}
        >
          {message.content && (
            <p className="text-sm leading-relaxed">{message.content}</p>
          )}

          {message.attachedFileUrls.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {message.attachedFileUrls.map((url) => (
                <img
                  key={url}
                  src={url}
                  alt="attachment"
                  className="rounded-xl max-w-[200px] border border-outline-variant/10"
                />
              ))}
            </div>
          )}
        </div>

        {/* Time + read */}
        <div className={`flex items-center gap-1 px-1 ${isOwnMessage ? 'flex-row-reverse' : ''}`}>
          <span className="text-[10px] text-slate-400">
            {formatRelativeTime(message.sentAt)}
          </span>
          {message.isRead && isOwnMessage && (
            <span
              className="material-symbols-outlined text-primary"
              style={{ fontSize: '12px', fontVariationSettings: "'FILL' 1" }}
            >
              done_all
            </span>
          )}
        </div>
      </div>

      {/* Action buttons (own messages only) */}
      {isOwnMessage && (
        <div className="flex flex-col gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            className="p-1.5 rounded-full hover:bg-surface-container-high text-on-surface-variant transition-colors"
            onClick={() => onEdit?.(message)}
            aria-label="Chỉnh sửa tin nhắn"
          >
            <span className="material-symbols-outlined text-sm">edit</span>
          </button>
          <button
            className="p-1.5 rounded-full hover:bg-error-container/20 text-error transition-colors"
            onClick={() => onDelete?.(message.id)}
            aria-label="Xóa tin nhắn"
          >
            <span className="material-symbols-outlined text-sm">delete</span>
          </button>
        </div>
      )}
    </div>
  )
}
