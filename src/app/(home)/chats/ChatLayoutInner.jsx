"use client";

import { useState, useEffect } from "react";
import ChatList from "@/components/social-app-component/ChatList";
import ChatBox from "@/components/social-app-component/ChatBox";
import useAppStore from "@/store/ZustandStore";
import useIsMobile from "@/hooks/useIsMobile";
import { pageMetadata, usePageMetadata } from "@/utils/clientMetadata";
import { useSearchParams, useRouter } from "next/navigation";
import { MessageCircle } from "lucide-react";

function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center h-full gap-4 text-center px-8">
      <div className="w-24 h-24 rounded-full border-2 border-[var(--foreground)] flex items-center justify-center">
        <MessageCircle
          size={48}
          strokeWidth={1}
          className="text-[var(--foreground)]"
        />
      </div>
      <div>
        <h3 className="text-xl font-semibold text-[var(--foreground)] mb-1">
          Tin nhắn của bạn
        </h3>
        <p className="text-sm text-[var(--muted-foreground)]">
          Gửi tin nhắn riêng tư cho bạn bè.
        </p>
      </div>
    </div>
  );
}

export default function ChatLayoutInner() {
  const [selectedChatId, setSelectedChatId] = useState(null);
  const [targetUser, setTargetUser] = useState(null);
  const [selectedChat, setSelectedChat] = useState(null); // ← FIX: thêm state
  const [chatListKey, setChatListKey] = useState(0);
  const [isInitializing, setIsInitializing] = useState(true);

  const searchParams = useSearchParams();
  const router = useRouter();
  const isMobile = useIsMobile();

  const chatIdFromUrl = searchParams.get("chatId");

  const chatList = useAppStore((state) => state.chatList);
  const fetchChatList = useAppStore((state) => state.fetchChatList);
  const clearChatSelection = useAppStore((state) => state.clearChatSelection);

  usePageMetadata(pageMetadata.chats());

  useEffect(() => {
    const timer = setTimeout(() => setIsInitializing(false), 100);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (chatIdFromUrl && chatList.length > 0) {
      const found = chatList.find(
        (chat) => chat.chatId === chatIdFromUrl || chat.id === chatIdFromUrl
      );
      if (found) {
        setSelectedChatId(chatIdFromUrl);
        setTargetUser(found.target);
        setSelectedChat(found); // ← FIX: set state
      }
    } else if (!chatIdFromUrl) {
      setSelectedChatId(null);
      setTargetUser(null);
      setSelectedChat(null); // ← FIX: reset state
    }
  }, [chatIdFromUrl, chatList]);

  const handleSelectChat = (chatId, user, chat) => {
    const params = new URLSearchParams(window.location.search);
    params.set("chatId", chatId);
    router.push(`/chats?${params.toString()}`, { scroll: false });
    setSelectedChatId(chatId);
    setTargetUser(user);
    setSelectedChat(chat); // ← FIX: set state
  };

  const handleChatCreated = async (newChatId, user) => {
    try {
      await fetchChatList();
      const params = new URLSearchParams(window.location.search);
      params.set("chatId", newChatId);
      router.push(`/chats?${params.toString()}`, { scroll: false });
      setSelectedChatId(newChatId);
      setTargetUser(user);
      setChatListKey((prev) => prev + 1);
    } catch (error) {
      console.error("Error in chat creation flow:", error);
    }
  };

  const handleBackToList = () => {
    const params = new URLSearchParams(window.location.search);
    params.delete("chatId");
    router.push(`/chats${params.toString() ? `?${params.toString()}` : ""}`, {
      scroll: false,
    });
    clearChatSelection();
    setSelectedChatId(null);
    setTargetUser(null);
    setSelectedChat(null); // ← FIX: reset state
  };

  if (isInitializing) {
    return (
      <div className="flex items-center justify-center" style={{ height: "100%" }}>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[var(--foreground)]" />
      </div>
    );
  }

  // Mobile
  if (isMobile) {
    if (targetUser) {
      return (
        <div className="fixed inset-0 z-50 bg-[var(--background)]">
          <ChatBox
            chatId={selectedChatId}
            targetUser={targetUser}
            chat={selectedChat}
            isGroup={selectedChat?.isGroup}
            onBack={handleBackToList}
            onChatCreated={handleChatCreated}
          />
        </div>
      );
    }
    return (
      <div className="flex flex-col h-full bg-[var(--background)]">
        <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--border)]">
          <span className="text-base font-semibold text-[var(--foreground)]">
            Danh sách trò chuyện
          </span>
        </div>
        <div className="flex-1 overflow-y-auto">
          <ChatList
            key={chatListKey}
            onSelectChat={handleSelectChat}
            selectedChatId={selectedChatId}
          />
        </div>
      </div>
    );
  }

  // Desktop
  return (
    <div
      className="flex bg-[var(--background)] text-[var(--foreground)]"
      style={{
        height: "100vh",
        marginLeft: "-9999px",
        paddingLeft: "9999px",
        marginRight: "-9999px",
        paddingRight: "9999px",
        overflow: "hidden",
      }}
    >
      {/* Left panel */}
      <div
        className="flex flex-col border-r border-[var(--border)] flex-shrink-0 h-full overflow-hidden"
        style={{ width: "397px" }}
      >
        <div className="flex items-center justify-between px-6 py-5 border-b border-[var(--border)]">
          <span className="text-base font-bold text-[var(--foreground)]"></span>
        </div>
        <div className="flex items-center justify-between px-6 py-3">
          <span className="text-base font-semibold text-[var(--foreground)]">
            Tin nhắn
          </span>
        </div>
        <div className="flex-1 overflow-y-auto">
          <ChatList
            key={chatListKey}
            onSelectChat={handleSelectChat}
            selectedChatId={selectedChatId}
          />
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex flex-col overflow-hidden h-full">
        {targetUser ? (
          <ChatBox
            chatId={selectedChatId}
            targetUser={targetUser}
            chat={selectedChat}
            isGroup={selectedChat?.isGroup}
            onBack={handleBackToList}
            onChatCreated={handleChatCreated}
          />
        ) : (
          <EmptyState />
        )}
      </div>
    </div>
  );
}