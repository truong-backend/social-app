"use client";

import { useState, useCallback } from "react";
import ChatList from "@/components/social-app-component/ChatList";
import ChatBox from "@/components/social-app-component/ChatBox";
import { MessageSquare } from "lucide-react";
import useAppStore from "@/store/ZustandStore";

export default function ChatPage() {
  const [selectedChatId, setSelectedChatId] = useState(null);
  const [selectedTargetUser, setSelectedTargetUser] = useState(null);
  const [selectedChat, setSelectedChat] = useState(null); // full chat object

  const { selectChat } = useAppStore();

  // ── Khi chọn 1 chat từ ChatList ──────────────────────────────────────────
  const handleSelectChat = useCallback((chatId, targetUser, chat) => {
    setSelectedChatId(chatId);
    setSelectedTargetUser(targetUser || null);
    setSelectedChat(chat || null);
    selectChat(chatId);
  }, [selectChat]);

  // ── Khi back từ ChatBox (mobile) ─────────────────────────────────────────
  const handleBack = useCallback(() => {
    setSelectedChatId(null);
    setSelectedTargetUser(null);
    setSelectedChat(null);
  }, []);

  // ── Sau khi tạo chat mới ─────────────────────────────────────────────────
  const handleChatCreated = useCallback((newChatId) => {
    setSelectedChatId(newChatId);
  }, []);

  const isGroup = selectedChat?.isGroup === true;

  return (
    <div className="flex h-full bg-[var(--background)]">
      {/* ── Left panel: Chat list ───────────────────────────────────────────── */}
      <div
        className={`
          ${selectedChatId ? "hidden md:flex" : "flex"}
          w-full md:w-[340px] lg:w-[380px]
          flex-col border-r border-[var(--border)] h-full overflow-hidden
          bg-[var(--card)]
        `}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--border)]">
          <h1 className="text-xl font-bold text-[var(--foreground)]">Tin nhắn</h1>
        </div>

        <div className="flex-1 overflow-hidden">
          <ChatList
            onSelectChat={handleSelectChat}
            selectedChatId={selectedChatId}
          />
        </div>
      </div>

      {/* ── Right panel: ChatBox ────────────────────────────────────────────── */}
      <div
        className={`
          ${selectedChatId ? "flex" : "hidden md:flex"}
          flex-1 h-full overflow-hidden
        `}
      >
        {selectedChatId ? (
          <ChatBox
            key={selectedChatId}
            chatId={selectedChatId}
            targetUser={selectedTargetUser}
            chat={selectedChat}
            isGroup={isGroup}
            onBack={handleBack}
            onChatCreated={handleChatCreated}
          />
        ) : (
          // Empty state khi chưa chọn chat (desktop only)
          <div className="hidden md:flex flex-1 flex-col items-center justify-center gap-4 text-[var(--muted-foreground)]">
            <div className="w-20 h-20 rounded-2xl bg-[var(--accent)] flex items-center justify-center">
              <MessageSquare className="w-10 h-10 text-[var(--muted-foreground)] opacity-50" />
            </div>
            <div className="text-center">
              <p className="text-lg font-semibold text-[var(--foreground)]">
                Chọn một đoạn chat
              </p>
              <p className="text-sm mt-1">
                Chọn từ danh sách bên trái để bắt đầu trò chuyện
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}