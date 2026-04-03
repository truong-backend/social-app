import { useState, useEffect, useRef } from 'react'
import { useShallow } from 'zustand/react/shallow'
import { useChatMessages } from '../hooks/useChatMessages'
import { useSendMessage } from '../hooks/useSendMessage'
import { useChatWebSocket } from '../hooks/useChatWebSocket'
import { useChatStore } from '../store/chat.store'
import { MessageBubble } from './MessageBubble'
import { useSessionStore } from '@stores/session.store'
import { CallButton } from '@features/call/components/CallButton'

interface ChatWindowProps {
  chatId:         string
  targetUserId:   string
  targetUsername: string
}

export const ChatWindow = ({ chatId, targetUserId, targetUsername }: ChatWindowProps) => {
  const [inputContent, setInputContent] = useState('')
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const bottomRef    = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const userId            = useSessionStore((state) => state.userId) ?? ''
  const messagesFromStore = useChatStore(
    useShallow((state) => state.messagesByChatId[chatId] ?? [])
  )

  useChatWebSocket(chatId)

  const { data, fetchNextPage, hasNextPage, isLoading } = useChatMessages(chatId)
  const sendMessage = useSendMessage(targetUserId, chatId)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messagesFromStore.length])

  const handleSend = (event: React.FormEvent) => {
    event.preventDefault()
    if (!inputContent.trim() && selectedFiles.length === 0) return
    sendMessage.mutate(
      { payload: { content: inputContent }, files: selectedFiles },
      { onSuccess: () => { setInputContent(''); setSelectedFiles([]) } },
    )
  }

  const queryMessages = data?.pages.flat() ?? []
  const allMessages   = [...queryMessages, ...messagesFromStore].filter(
    (msg, index, self) => self.findIndex((m) => m.id === msg.id) === index,
  )

  return (
    <div className="flex flex-col flex-1 h-full bg-surface-container-low">
      {/* Header */}
      <header className="h-20 flex items-center justify-between px-8 bg-surface-bright/70 backdrop-blur-xl sticky top-0 z-10 border-b border-outline-variant/10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold text-sm">
            {targetUsername.charAt(0).toUpperCase()}
          </div>
          <div>
            <h2 className="font-bold text-on-surface leading-tight">{targetUsername}</h2>
            <p className="text-[11px] text-primary font-semibold flex items-center gap-1">
              <span className="w-1.5 h-1.5 bg-secondary rounded-full" />
              Đang hoạt động
            </p>
          </div>
        </div>

        {targetUserId && (
          <CallButton targetUserId={targetUserId} targetName={targetUsername} />
        )}
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-8 py-6 space-y-4">
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

        {isLoading ? (
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
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input area */}
      <footer className="p-6 bg-surface-bright border-t border-outline-variant/10">
        {/* File previews */}
        {selectedFiles.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-3">
            {selectedFiles.map((file, index) => (
              <span
                key={index}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-surface-container-high text-on-surface text-xs rounded-full font-medium"
              >
                <span className="material-symbols-outlined text-sm text-primary">attach_file</span>
                {file.name}
                <button
                  type="button"
                  className="ml-1 text-on-surface-variant hover:text-error transition-colors"
                  onClick={() => setSelectedFiles((prev) => prev.filter((_, i) => i !== index))}
                >
                  <span className="material-symbols-outlined text-sm">close</span>
                </button>
              </span>
            ))}
          </div>
        )}

        <form
          className="flex items-center gap-3 bg-surface-container-low p-2 rounded-2xl"
          onSubmit={handleSend}
        >
          {/* Attach */}
          <div className="flex items-center gap-1">
            <button
              type="button"
              className="w-10 h-10 flex items-center justify-center text-primary hover:bg-surface-container-high rounded-xl transition-colors"
              onClick={() => fileInputRef.current?.click()}
            >
              <span className="material-symbols-outlined">add_circle</span>
            </button>
            <button
              type="button"
              className="w-10 h-10 flex items-center justify-center text-on-surface-variant hover:bg-surface-container-high rounded-xl transition-colors"
              onClick={() => fileInputRef.current?.click()}
            >
              <span className="material-symbols-outlined">image</span>
            </button>
          </div>

          <input
            ref={fileInputRef}
            type="file"
            multiple
            className="hidden"
            onChange={(e) =>
              setSelectedFiles((prev) => [...prev, ...Array.from(e.target.files ?? [])])
            }
          />

          <input
            className="flex-1 bg-transparent border-none focus:ring-0 text-sm text-on-surface placeholder:text-on-surface-variant/60 outline-none"
            type="text"
            placeholder="Nhập tin nhắn..."
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
            <button
              type="button"
              className="w-10 h-10 flex items-center justify-center text-on-surface-variant hover:bg-surface-container-high rounded-xl transition-colors"
            >
              <span className="material-symbols-outlined">sentiment_satisfied</span>
            </button>
            <button
              type="submit"
              className="w-10 h-10 flex items-center justify-center bg-primary text-on-primary rounded-xl shadow-md shadow-primary/20 active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none"
              disabled={sendMessage.isPending || (!inputContent.trim() && selectedFiles.length === 0)}
            >
              <span className="material-symbols-outlined">send</span>
            </button>
          </div>
        </form>
      </footer>
    </div>
  )
}