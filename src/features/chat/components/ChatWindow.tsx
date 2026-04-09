import { useState, useEffect, useRef } from 'react'
import { useShallow } from 'zustand/react/shallow'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useChatMessages } from '../hooks/useChatMessages'
import { useSendMessage } from '../hooks/useSendMessage'
import { useChatWebSocket } from '../hooks/useChatWebSocket'
import { useChatStore } from '../store/chat.store'
import { MessageBubble } from './MessageBubble'
import { useSessionStore } from '@stores/session.store'
import { deleteMessageApi, updateMessageApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS } from '../constants/chat.constants'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'
import type { Message } from '../types/chat.types'

interface ChatWindowProps {
  // FIX: chatId có thể null khi mở chat mới từ trang profile (chưa gửi tin nào)
  chatId:           string | null
  targetUserId:     string
  targetUsername:   string
  targetProfilePic?: string
}

export const ChatWindow = ({ chatId, targetUserId, targetUsername, targetProfilePic }: ChatWindowProps) => {
  const [inputContent, setInputContent] = useState('')
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const [editingMessage, setEditingMessage] = useState<Message | null>(null)
  const [editContent, setEditContent] = useState('')
  // FIX: theo dõi chatId thực sau khi gửi tin đầu tiên (chat mới tạo)
  const [resolvedChatId, setResolvedChatId] = useState<string | null>(chatId)

  const bottomRef    = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const editInputRef = useRef<HTMLInputElement>(null)

  const userId = useSessionStore((state) => state.userId) ?? ''

  // FIX: đồng bộ resolvedChatId khi prop chatId thay đổi (user chọn chat khác)
  useEffect(() => {
    setResolvedChatId(chatId)
  }, [chatId])

  // FIX: đọc store với resolvedChatId (UUID thật), không phải prop chatId (có thể null)
  const messagesFromStore = useChatStore(
    useShallow((state) => state.messagesByChatId[resolvedChatId ?? ''] ?? [])
  )
  const { updateMessage: updateStoreMessage, removeMessage, setActiveChatId } = useChatStore()

  const queryClient = useQueryClient()

  // FIX: chỉ subscribe WS khi đã có chatId thật
  useChatWebSocket(resolvedChatId ?? '')

  // FIX: chỉ fetch messages khi có chatId thật
  const { data, fetchNextPage, hasNextPage, isLoading } = useChatMessages(resolvedChatId ?? '')

  const sendMessage = useSendMessage(targetUserId, resolvedChatId)

  useEffect(() => {
    if (editingMessage) {
      setTimeout(() => editInputRef.current?.focus(), 50)
    }
  }, [editingMessage])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messagesFromStore.length])

  const deleteMessage = useMutation({
    mutationFn: (messageId: string) => deleteMessageApi(messageId, { type: 'EVERY' }),
    onSuccess: (_, messageId) => {
      removeMessage(resolvedChatId ?? '', messageId)
      if (resolvedChatId) {
        queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.messages(resolvedChatId) })
      }
    },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })

  const editMessage = useMutation({
    mutationFn: ({ messageId, content }: { messageId: string; content: string }) =>
      updateMessageApi(messageId, { content }),
    onSuccess: (updated) => {
      updateStoreMessage(resolvedChatId ?? '', updated)
      if (resolvedChatId) {
        queryClient.invalidateQueries({ queryKey: CHAT_QUERY_KEYS.messages(resolvedChatId) })
      }
      setEditingMessage(null)
      setEditContent('')
    },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })

  const handleSend = (event: React.FormEvent) => {
    event.preventDefault()
    if (!inputContent.trim() && selectedFiles.length === 0) return
    sendMessage.mutate(
      { payload: { content: inputContent }, files: selectedFiles },
      {
        onSuccess: (newMessage) => {
          setInputContent('')
          setSelectedFiles([])
          // FIX: cập nhật resolvedChatId nếu là chat mới vừa tạo
          if (!resolvedChatId && newMessage.chatId) {
            setResolvedChatId(newMessage.chatId)
            setActiveChatId(newMessage.chatId)
          }
        },
      },
    )
  }

  const handleEditSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!editContent.trim() || !editingMessage) return
    editMessage.mutate({ messageId: editingMessage.id, content: editContent })
  }

  const handleStartEdit = (message: Message) => {
    setEditingMessage(message)
    setEditContent(message.content)
  }

  const handleDelete = (messageId: string) => {
    if (window.confirm('Xóa tin nhắn này?')) {
      deleteMessage.mutate(messageId)
    }
  }

  const queryMessages = data?.pages.flat() ?? []

  // FIX: merge đúng thứ tự — queryMessages (đã load từ DB) + messagesFromStore (mới gửi/WS)
  // dedup theo id, ưu tiên bản từ store vì có attachedFileUrls đầy đủ hơn bản cache cũ
  const allMessages = [...messagesFromStore, ...queryMessages].filter(
    (msg, index, self) => self.findIndex((m) => m.id === msg.id) === index,
  ).sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())

  return (
    <div className="flex flex-col flex-1 h-full bg-surface">
      {/* Header */}
      <div className="px-8 py-4 bg-white/70 backdrop-blur-md flex items-center justify-between border-b border-slate-100 sticky top-0 z-10">
        <div className="flex items-center gap-4">
          {targetProfilePic ? (
            <img src={targetProfilePic} alt={targetUsername} className="w-10 h-10 rounded-full object-cover" />
          ) : (
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold text-sm">
              {targetUsername.charAt(0).toUpperCase()}
            </div>
          )}
          <div>
            <h3 className="font-bold text-on-surface leading-tight" style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
              {targetUsername}
            </h3>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 bg-tertiary-fixed-dim rounded-full" />
              <span className="text-[11px] text-on-surface-variant font-medium">Online now</span>
            </div>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-8 py-6 space-y-6" style={{ scrollbarWidth: 'thin' }}>
        {hasNextPage && (
          <div className="flex justify-center">
            <button
              className="px-4 py-2 rounded-full bg-surface-container-low text-primary text-xs font-bold hover:bg-surface-container-high transition-colors"
              onClick={() => fetchNextPage()}
            >
              Tải tin nhắn cũ hơn
            </button>
          </div>
        )}

        {isLoading && resolvedChatId ? (
          <div className="flex items-center justify-center py-12 text-on-surface-variant text-sm gap-2">
            <span className="w-5 h-5 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
            Đang tải...
          </div>
        ) : (
          allMessages.map((message) => (
            <MessageBubble
              key={message.id}
              message={message}
              isOwnMessage={message.senderId === userId}
              onDelete={handleDelete}
              onEdit={handleStartEdit}
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      {/* Edit mode banner */}
      {editingMessage && (
        <div className="mx-6 mb-2 bg-primary/5 border border-primary/20 rounded-xl px-4 py-2 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-sm">edit</span>
            <span className="text-xs text-primary font-semibold">Đang chỉnh sửa tin nhắn</span>
          </div>
          <button
            onClick={() => { setEditingMessage(null); setEditContent('') }}
            className="text-on-surface-variant hover:text-error transition-colors"
          >
            <span className="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
      )}

      {/* Input area */}
      <footer className="p-6 bg-white/40">
        {/* File previews — hiện thumbnail ảnh thay vì tên file */}
        {selectedFiles.length > 0 && !editingMessage && (
          <div className="flex flex-wrap gap-2 mb-3">
            {selectedFiles.map((file, index) => (
              <div key={index} className="relative">
                {file.type.startsWith('image/') ? (
                  <img
                    src={URL.createObjectURL(file)}
                    alt={file.name}
                    className="w-16 h-16 object-cover rounded-xl border border-outline-variant/20"
                  />
                ) : (
                  <span className="flex items-center gap-1.5 px-3 py-1.5 bg-surface-container-high text-on-surface text-xs rounded-full font-medium">
                    <span className="material-symbols-outlined text-sm text-primary">attach_file</span>
                    {file.name}
                  </span>
                )}
                <button
                  type="button"
                  className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-error text-white rounded-full flex items-center justify-center text-[9px]"
                  onClick={() => setSelectedFiles((prev) => prev.filter((_, i) => i !== index))}
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        )}

        {editingMessage ? (
          <form className="flex items-center gap-2 bg-surface-container-lowest rounded-2xl p-2 border border-primary/30 shadow-sm" onSubmit={handleEditSubmit}>
            <input
              ref={editInputRef}
              className="flex-1 bg-transparent border-none focus:ring-0 text-sm text-on-surface outline-none py-2 px-2"
              type="text"
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Escape') { setEditingMessage(null); setEditContent('') }
              }}
            />
            <button type="submit" className="p-2 bg-primary text-white rounded-xl shadow-md active:scale-90 transition-transform disabled:opacity-50"
              disabled={editMessage.isPending || !editContent.trim()}>
              <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>check</span>
            </button>
          </form>
        ) : (
          <form className="flex items-center gap-2 bg-surface-container-lowest rounded-2xl p-2 border border-outline-variant/15 shadow-sm focus-within:ring-2 ring-primary-fixed" onSubmit={handleSend}>
            <div className="flex items-center gap-1">
              <button type="button" className="p-2 hover:bg-slate-50 rounded-xl transition-colors text-slate-400" onClick={() => fileInputRef.current?.click()}>
                <span className="material-symbols-outlined">add_circle</span>
              </button>
            </div>

            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept="image/*,video/*,application/pdf"
              className="hidden"
              onChange={(e) => {
                setSelectedFiles((prev) => [...prev, ...Array.from(e.target.files ?? [])])
                e.target.value = ''
              }}
            />

            <input
              className="flex-1 bg-transparent border-none focus:ring-0 text-sm text-on-surface placeholder:text-on-surface-variant/60 outline-none py-2"
              type="text"
              placeholder="Type a message..."
              value={inputContent}
              onChange={(e) => setInputContent(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  handleSend(e as unknown as React.FormEvent)
                }
              }}
            />

            <div className="flex items-center gap-1">
              <button type="button" className="p-2 hover:bg-slate-50 rounded-xl transition-colors text-slate-400">
                <span className="material-symbols-outlined">sentiment_satisfied</span>
              </button>
              <button type="submit"
                className="p-2 bg-primary text-white rounded-xl shadow-md active:scale-90 transition-transform disabled:opacity-50 disabled:pointer-events-none"
                disabled={sendMessage.isPending || (!inputContent.trim() && selectedFiles.length === 0)}>
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>send</span>
              </button>
            </div>
          </form>
        )}

        <p className="text-center text-[10px] text-slate-400 mt-3 font-medium uppercase tracking-widest">
          End-to-end encrypted
        </p>
      </footer>
    </div>
  )
}