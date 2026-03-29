import type { Message } from '../types/chat.types'
import { formatRelativeTime } from '@utils/date.formatter'

interface MessageBubbleProps {
  message: Message
  isOwnMessage: boolean
  onDelete?: (messageId: string) => void
  onEdit?: (message: Message) => void
}

export const MessageBubble = ({
  message,
  isOwnMessage,
  onDelete,
  onEdit,
}: MessageBubbleProps) => {
  return (
    <div
      className={`message-bubble ${
        isOwnMessage ? 'message-bubble--own' : 'message-bubble--other'
      }`}
    >
      <div className="message-bubble__content">
        {message.content && (
          <p className="message-bubble__text">{message.content}</p>
        )}

        {message.attachedFileUrls.length > 0 && (
          <div className="message-bubble__attachments">
            {message.attachedFileUrls.map((url) => (
              <img
                key={url}
                src={url}
                alt="attachment"
                className="message-bubble__attachment-img"
              />
            ))}
          </div>
        )}

        <span className="message-bubble__time">
          {formatRelativeTime(message.sentAt)}
          {message.isRead && isOwnMessage && (
            <span className="message-bubble__read-indicator"> ✓✓</span>
          )}
        </span>
      </div>

      {isOwnMessage && (
        <div className="message-bubble__actions">
          <button
            className="message-bubble__action-btn"
            onClick={() => onEdit?.(message)}
            aria-label="Chỉnh sửa tin nhắn"
          >
            ✏️
          </button>
          <button
            className="message-bubble__action-btn message-bubble__action-btn--danger"
            onClick={() => onDelete?.(message.id)}
            aria-label="Xóa tin nhắn"
          >
            🗑️
          </button>
        </div>
      )}
    </div>
  )
}