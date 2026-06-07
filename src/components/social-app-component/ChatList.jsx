"use client";
import { usePathname } from "next/navigation";
import { ChevronDown, ChevronUp, SearchIcon, RefreshCw, Users } from "lucide-react";
import ChatItem from "./ChatItem";
import Input from "../ui-components/Input";
import Avatar from "../ui-components/Avatar";
import { useEffect, useRef, useState } from "react";
import api from "@/utils/axios";
import useAppStore from "@/store/ZustandStore";
import CreateGroupModal from "./CreateGroupModal";

export default function ChatList({ onSelectChat, selectedChatId }) {
  const pathname = usePathname();

  const {
    chatList,
    isLoadingChats,
    fetchChatList,
    markChatAsRead,
    refreshChatList,
    error: storeError,
  } = useAppStore();

  const [expanded, setExpanded] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState(null);
  const [isSearching, setIsSearching] = useState(false);
  const [fetchAttempted, setFetchAttempted] = useState(false);
  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const listRef = useRef(null);
  const isChatsPage = pathname === "/chats";

  // ── Debug ─────────────────────────────────────────────────────────────────
  useEffect(() => {
    console.log("🔍 ChatList Debug:", {
      chatListLength: chatList.length,
      isLoadingChats,
      fetchAttempted,
      storeError,
    });
  }, [chatList.length, isLoadingChats, fetchAttempted, storeError]);

  // ── Fetch ─────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (!fetchAttempted && typeof fetchChatList === "function") {
      setFetchAttempted(true);
      fetchChatList()
        .then((data) =>
          console.log("✅ Chat list fetched:", data?.length || 0, "chats")
        )
        .catch((err) => {
          console.error("❌ Failed to fetch:", err);
          setFetchAttempted(false);
        });
    }
  }, [fetchAttempted, fetchChatList]);

  // ── Auto expand on chats page ─────────────────────────────────────────────
  useEffect(() => {
    if (isChatsPage) setExpanded(true);
  }, [isChatsPage]);

  // ── Scroll top on new message ─────────────────────────────────────────────
  useEffect(() => {
    if (listRef.current && chatList.length > 0) {
      listRef.current.scrollTo({ top: 0, behavior: "smooth" });
    }
  }, [chatList]);

  // ── Handlers ──────────────────────────────────────────────────────────────
  const handleChatSelect = async (chat) => {
    if (typeof markChatAsRead !== "function") return;
    const chatId = chat.chatId || chat.id;
    try {
      await markChatAsRead(chatId);
      onSelectChat(chatId, chat.target, chat);
    } catch (err) {
      console.error("❌ Error selecting chat:", err);
    }
  };

  const handleRefresh = async () => {
    if (typeof refreshChatList !== "function") return;
    setFetchAttempted(false);
    try {
      await refreshChatList();
    } catch (err) {
      console.error("❌ Refresh failed:", err);
    }
  };

  // ── Debounced search ──────────────────────────────────────────────────────
  useEffect(() => {
    if (!searchTerm) { setSearchResults(null); return; }
    const timeout = setTimeout(async () => {
      try {
        setIsSearching(true);
        const res = await api.get(`/v1/chat/search`, { params: { query: searchTerm } });
        setSearchResults(res.data.body || res.data || []);
      } catch (err) {
        console.error("Search failed:", err);
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    }, 1000);
    return () => clearTimeout(timeout);
  }, [searchTerm]);

  const filteredChats = searchResults ?? chatList;

  // ── Collapsed state avatars ───────────────────────────────────────────────
  const uniqueChats = [
    ...new Map(
      chatList.map((chat) => [
        chat.isGroup
          ? chat.chatId
          : chat.target?.userId || chat.target?.id || chat.target?.username,
        chat,
      ])
    ).values(),
  ];
  const onlineCount = uniqueChats.filter(
    (chat) => !chat.isGroup && chat.target?.onlineStatus?.isOnline
  ).length;

  // ── Loading ───────────────────────────────────────────────────────────────
  if (isLoadingChats) {
    return (
      <div className="space-y-3 p-4 animate-pulse">
        <div className="flex items-center justify-between">
          <div className="h-6 bg-muted rounded w-1/3" />
          <div className="h-6 w-6 bg-muted rounded" />
        </div>
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-14 bg-muted rounded-lg" />
        ))}
      </div>
    );
  }

  // ── Error ─────────────────────────────────────────────────────────────────
  if (storeError) {
    return (
      <div className="p-4 text-center text-sm">
        <div className="text-destructive mb-2">Không thể tải đoạn chat</div>
        <div className="text-muted-foreground text-xs mb-3">{storeError}</div>
        <button
          onClick={handleRefresh}
          disabled={isLoadingChats}
          className="flex items-center gap-2 mx-auto px-3 py-1.5 text-primary hover:bg-primary/10 rounded-md transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${isLoadingChats ? "animate-spin" : ""}`} />
          {isLoadingChats ? "Đang thử lại..." : "Thử lại"}
        </button>
      </div>
    );
  }

  // ── Empty ─────────────────────────────────────────────────────────────────
  if (!isLoadingChats && chatList.length === 0 && fetchAttempted) {
    return (
      <div className="p-4 text-center text-muted-foreground">
        <div className="mb-2">Chưa có cuộc trò chuyện nào</div>
        <button
          onClick={handleRefresh}
          className="flex items-center gap-2 mx-auto px-3 py-1.5 text-primary hover:bg-primary/10 rounded-md transition-colors text-sm"
        >
          <RefreshCw className="h-4 w-4" />
          Tải lại
        </button>
      </div>
    );
  }

  // ── Collapsed ─────────────────────────────────────────────────────────────
  if (!expanded && !isChatsPage) {
    return (
      <div
        role="button"
        onClick={() => setExpanded(true)}
        className="w-full md:w-[300px] max-w-md mx-auto flex items-center justify-between p-2 md:p-3 bg-background border rounded-full cursor-pointer hover:bg-accent transition-colors"
      >
        <div className="flex -space-x-2">
          {uniqueChats
            .slice()
            .reverse()
            .slice(0, 3)
            .map((chat, i) => (
              <div key={i} className="relative">
                {chat.isGroup ? (
                  <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 border-2 border-background flex items-center justify-center">
                    {chat.groupAvatarUrl ? (
                      <img src={chat.groupAvatarUrl} alt="" className="w-full h-full object-cover rounded-full" />
                    ) : (
                      <span className="text-white text-xs font-bold">{chat.name?.[0]}</span>
                    )}
                  </div>
                ) : (
                  <Avatar
                    src={chat.target?.profilePictureUrl}
                    alt={`${chat.target?.givenName} ${chat.target?.familyName}`}
                    className="border-2 border-background w-8 h-8 md:w-10 md:h-10"
                  />
                )}
                {!chat.isGroup && chat.target?.onlineStatus?.isOnline && (
                  <div className="absolute bottom-0 right-0">
                    <div className="w-2.5 h-2.5 md:w-3 md:h-3 bg-green-500 rounded-full border border-background">
                      <div className="absolute inset-0 bg-green-500 rounded-full animate-pulse opacity-75" />
                    </div>
                  </div>
                )}
              </div>
            ))}
        </div>
        <div className="flex items-center gap-1 md:gap-2 ml-2">
          {onlineCount > 0 && (
            <div className="flex items-center gap-1 text-xs text-muted-foreground">
              <div className="w-2 h-2 bg-green-500 rounded-full" />
              <span className="hidden sm:inline">{onlineCount} online</span>
              <span className="sm:hidden">{onlineCount}</span>
            </div>
          )}
          <span className="text-xs md:text-sm text-muted-foreground hidden md:inline">
            {chatList.length > 3
              ? `và +${chatList.length - 3} đoạn chat khác`
              : `${chatList.length} cuộc trò chuyện`}
          </span>
          <span className="text-xs text-muted-foreground md:hidden">{chatList.length}</span>
          <ChevronDown className="h-3 w-3 md:h-4 md:w-4 text-muted-foreground" />
        </div>
      </div>
    );
  }

  // ── Expanded ──────────────────────────────────────────────────────────────
  return (
    <>
      <div className="w-full md:max-w-md mx-auto bg-background flex flex-col border rounded-lg overflow-hidden h-full shadow-sm">
        {/* Header — chỉ hiển thị khi không phải chats page */}
        {!isChatsPage && (
          <div className="flex items-center justify-between p-2 md:p-3 border-b">
            <div className="flex items-center gap-2">
              <h3 className="font-medium text-xs md:text-sm">Tin nhắn</h3>
              {onlineCount > 0 && (
                <div className="flex items-center gap-1 text-xs text-green-600">
                  <div className="w-2 h-2 bg-green-500 rounded-full" />
                  <span>{onlineCount} online</span>
                </div>
              )}
            </div>

            <div className="flex items-center gap-1">
              {/* ── NÚT TẠO NHÓM ── */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowCreateGroup(true);
                }}
                className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700 px-2 py-1 rounded-lg hover:bg-blue-50 transition-colors"
                title="Tạo nhóm chat mới"
              >
                <Users className="h-4 w-4" />
                <span className="hidden sm:inline">Tạo nhóm</span>
              </button>

              <button
                onClick={handleRefresh}
                disabled={isLoadingChats}
                className="text-muted-foreground hover:text-foreground transition-colors disabled:opacity-50 p-1"
                aria-label="Refresh chats"
              >
                <RefreshCw className={`h-4 w-4 ${isLoadingChats ? "animate-spin" : ""}`} />
              </button>

              <button
                onClick={() => setExpanded(false)}
                className="text-muted-foreground hover:text-foreground transition-colors p-1"
                aria-label="Collapse chat list"
              >
                <ChevronUp className="h-4 w-4 md:h-5 md:w-5" />
              </button>
            </div>
          </div>
        )}

        {/* Search */}
        <div className="px-2 md:px-3 py-1 md:py-2 border-b hidden md:block">
          <div className="relative">
            <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm đoạn chat"
              className="w-full pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        {/* List */}
        <div
          ref={listRef}
          className="flex-1 overflow-y-auto"
          style={{
            maxHeight: isChatsPage
              ? "none"
              : typeof window !== "undefined" && window.innerWidth < 768
              ? "250px"
              : "400px",
          }}
        >
          {filteredChats.length > 0 ? (
            [...filteredChats].reverse().map((chat) => (
              <div key={chat.chatId}>
                <ChatItem
                  chat={chat}
                  selected={selectedChatId === chat.chatId}
                  onClick={() => handleChatSelect(chat)}
                />
              </div>
            ))
          ) : (
            <div className="p-2 md:p-4 text-center text-muted-foreground text-xs md:text-sm">
              {isSearching
                ? "Đang tìm kiếm..."
                : searchTerm
                ? "Không có kết quả"
                : "Không có đoạn chat nào"}
            </div>
          )}
        </div>
      </div>

      {/* Modal tạo nhóm chat */}
      {showCreateGroup && (
        <CreateGroupModal
          onClose={() => setShowCreateGroup(false)}
          onCreated={() => {
            setShowCreateGroup(false);
            handleRefresh();
          }}
        />
      )}
    </>
  );
}