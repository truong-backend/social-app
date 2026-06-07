import { create } from "zustand";
import { devtools } from "zustand/middleware";
import api from "@/utils/axios";

// ── Event constants ──────────────────────────────────────────────────────────
export const STORE_EVENTS = {
  CHAT_LIST_LOAD: "chat_list_load",
  CHAT_CREATED: "chat_created",
  MESSAGE_RECEIVED: "message_received",
  NOTIFICATION_RECEIVED: "notification_received",
  NOTIFICATIONS_LOAD: "notifications_load",
  UNREAD_COUNT_LOAD: "unread_count_load",
  NEWSFEED_LOAD: "newsfeed_load",
  POST_CREATED: "post_created",
  SEARCH_PERFORMED: "search_performed",
  UNREAD_MESSAGE_COUNT_UPDATED: "unread_message_count_updated",
  BLOCK_STATUS_UPDATED: "block_status_updated",
};

const useAppStore = create(
  devtools(
    (set, get) => ({
      // ════════════════════════════════════════════════
      // USER STATE
      // ════════════════════════════════════════════════
      userName: null,
      setUserNameStore: (username) => set({ userName: username }),
      getUserNameStore: () => get().userName,

      filterType: "RELEVANT",
      setFilterType: (filterType) => set({ filterType }),

      // ════════════════════════════════════════════════
      // CHAT STATE
      // ════════════════════════════════════════════════
      chatList: [],
      conversationMap: new Map(),
      isLoadingChats: false,
      error: null,
      unreadMessageCount: 0,

      // ── Tính số tin nhắn chưa đọc ──────────────────
      calculateUnreadMessageCount: (chatList) =>
        chatList.reduce((sum, chat) => sum + (chat.notReadMessageCount || 0), 0),

      updateUnreadMessageCount: () => {
        const { chatList } = get();
        const newCount = get().calculateUnreadMessageCount(chatList);
        set({ unreadMessageCount: newCount });
        console.log(`✅ ${STORE_EVENTS.UNREAD_MESSAGE_COUNT_UPDATED} - ${newCount}`);
        return newCount;
      },

      // ── Fetch chat list ─────────────────────────────
      fetchChatList: async () => {
        set({ isLoadingChats: true, error: null });
        try {
          console.log("🚀 Fetching chat list...");
          const res = await api.get("/v1/chat");
          const data = res.data.body || res.data || [];

          // Sắp xếp theo latestMessage.sentAt giảm dần
          const sorted = [...data].sort((a, b) => {
            const tA = a.latestMessage?.sentAt
              ? new Date(a.latestMessage.sentAt).getTime()
              : new Date(a.createdAt || 0).getTime();
            const tB = b.latestMessage?.sentAt
              ? new Date(b.latestMessage.sentAt).getTime()
              : new Date(b.createdAt || 0).getTime();
            return tB - tA;
          });

          const unreadCount = get().calculateUnreadMessageCount(sorted);
          set({ chatList: sorted, isLoadingChats: false, error: null, unreadMessageCount: unreadCount });

          console.log(`✅ ${STORE_EVENTS.CHAT_LIST_LOAD} - ${sorted.length} chats`);
          return sorted;
        } catch (error) {
          console.error("❌ Error fetching chats:", error);
          const msg = error.response?.data?.message || error.message || "Failed to load chats";
          set({ isLoadingChats: false, error: msg, chatList: [], unreadMessageCount: 0 });
          throw error;
        }
      },

      // ── Update online status ────────────────────────
      updateChatUserOnlineStatus: (userId, onlineStatusData) => {
        set((state) => {
          const updatedChatList = state.chatList.map((chat) => {
            if (!chat.isGroup && chat.target?.id === userId) {
              return {
                ...chat,
                target: {
                  ...chat.target,
                  isOnline: onlineStatusData.online,
                  lastOnline: onlineStatusData.lastOnline || null,
                },
              };
            }
            return chat;
          });
          const unreadCount = get().calculateUnreadMessageCount(updatedChatList);
          return { chatList: updatedChatList, unreadMessageCount: unreadCount };
        });
      },

      // ── Block status ────────────────────────────────
      getBlockStatusByChatId: (chatId) => {
        const { chatList } = get();
        const chat = chatList.find((c) => c.id === chatId || c.chatId === chatId);
        return chat?.blockStatus || "NORMAL";
      },

      // ── Mark as read ────────────────────────────────
      markChatAsRead: async (chatId) => {
        try {
          set((state) => {
            const updatedChatList = state.chatList.map((chat) =>
              chat.chatId === chatId || chat.id === chatId
                ? { ...chat, notReadMessageCount: 0 }
                : chat
            );
            const unreadCount = get().calculateUnreadMessageCount(updatedChatList);
            return { chatList: updatedChatList, unreadMessageCount: unreadCount };
          });
        } catch (error) {
          console.error("❌ Error marking as read:", error);
        }
      },

      // ── On message received ─────────────────────────
      onMessageReceived: (message, isCurrentChatOpen = false) => {
        set((state) => {
          const updatedChats = state.chatList
            .map((chat) => {
              if (chat.chatId === message.chatId || chat.id === message.chatId) {
                return {
                  ...chat,
                  latestMessage: message,
                  updatedAt: message.sentAt || message.createdAt,
                  notReadMessageCount: isCurrentChatOpen
                    ? 0
                    : (chat.notReadMessageCount || 0) + 1,
                };
              }
              return chat;
            })
            .sort((a, b) => {
              const tA = a.latestMessage?.sentAt
                ? new Date(a.latestMessage.sentAt).getTime()
                : new Date(a.updatedAt || a.createdAt || 0).getTime();
              const tB = b.latestMessage?.sentAt
                ? new Date(b.latestMessage.sentAt).getTime()
                : new Date(b.updatedAt || b.createdAt || 0).getTime();
              return tB - tA;
            });

          const unreadCount = get().calculateUnreadMessageCount(updatedChats);
          return { chatList: updatedChats, unreadMessageCount: unreadCount };
        });
      },

      // ── On chat created ─────────────────────────────
      onChatCreated: (newChat) => {
        set((state) => {
          const updatedChatList = [newChat, ...state.chatList];
          const unreadCount = get().calculateUnreadMessageCount(updatedChatList);
          return { chatList: updatedChatList, unreadMessageCount: unreadCount };
        });
      },

      // ── Group event handler ─────────────────────────
      onGroupEvent: (event) => {
        // Refresh chat list khi có sự kiện nhóm (thêm/xóa thành viên, đổi tên, v.v.)
        get().fetchChatList().catch(console.error);
      },

      // ── Selected chat (full object) ─────────────────
      selectedChat: null,
      selectChatFull: (chat) =>
        set({
          selectedChatId: chat?.chatId || chat?.id || null,
          selectedChat: chat || null,
          virtualChatUser: null,
        }),

      // ════════════════════════════════════════════════
      // NOTIFICATIONS STATE
      // ════════════════════════════════════════════════
      notifications: [],
      isLoadingNotifications: false,
      unreadNotificationCount: 0,
      unreadNotificationCountFromSocket: 0,

      fetchUnreadNotificationCount: async () => {
        try {
          const res = await api.get("/v1/notifications/unread-count");
          const unreadCount = res.data.body;
          set({ unreadNotificationCount: unreadCount, error: null });
          console.log(`✅ ${STORE_EVENTS.UNREAD_COUNT_LOAD} - ${unreadCount}`);
          return unreadCount;
        } catch (error) {
          console.error("❌ Error fetching unread count:", error);
          return 0;
        }
      },

      fetchNotifications: async (force = false, page = 0, size = 10) => {
        const { notifications, isLoadingNotifications } = get();
        if (!force && notifications.length > 0) return notifications;
        if (isLoadingNotifications) return notifications;

        set({ isLoadingNotifications: true, error: null });
        try {
          const res = await api.get("/v1/notifications", { params: { page, size } });
          const responseData = res.data.body?.notifications;
          let data = [];

          if (responseData) {
            if (responseData.body && Array.isArray(responseData.body)) data = responseData.body;
            else if (Array.isArray(responseData)) data = responseData;
          }

          const currentNotifications = get().notifications;
          let finalNotifications = data;
          if (currentNotifications.length > 0) {
            const apiIds = new Set(data.map((n) => n.id));
            const socketOnly = currentNotifications.filter((n) => !apiIds.has(n.id));
            finalNotifications = [...socketOnly, ...data];
          }

          set({ notifications: finalNotifications, isLoadingNotifications: false, error: null });
          return finalNotifications;
        } catch (error) {
          console.error("❌ Error fetching notifications:", error);
          const msg = error.response?.data?.message || error.message || "Failed";
          set({ isLoadingNotifications: false, error: msg });
          throw error;
        }
      },

      onNotificationReceived: (notification) => {
        const { notifications } = get();
        if (notifications.length === 0) {
          get().fetchNotifications(true).catch(console.error);
        }
        if (notifications.find((n) => n.id === notification.id)) return;
        set((state) => ({
          notifications: [notification, ...state.notifications],
          unreadNotificationCountFromSocket: state.unreadNotificationCountFromSocket + 1,
        }));
      },

      // ════════════════════════════════════════════════
      // CHAT NAVIGATION & SELECTION
      // ════════════════════════════════════════════════
      selectedChatId: null,
      virtualChatUser: null,

      selectChat: (chatId) => {
        set({ selectedChatId: chatId, virtualChatUser: null });
      },

      showVirtualChat: (userId, userInfo) => {
        set({ selectedChatId: null, virtualChatUser: { id: userId, ...userInfo } });
      },

      clearChatSelection: () => {
        set({ selectedChatId: null, virtualChatUser: null, selectedChat: null });
      },

      // ════════════════════════════════════════════════
      // INITIALIZATION
      // ════════════════════════════════════════════════
      initializeApp: async () => {
        console.log("🚀 Initializing app...");
        try {
          await Promise.allSettled([
            get().fetchChatList(),
            get().fetchUnreadNotificationCount(),
          ]);
          console.log("✅ App initialized");
        } catch (error) {
          console.error("❌ Init error:", error);
          set({ error: "Failed to initialize app" });
        }
      },

      // ════════════════════════════════════════════════
      // UTILITY
      // ════════════════════════════════════════════════
      clearAllData: () => {
        set(
          {
            chatList: [],
            filterType: "RELEVANT",
            conversationMap: new Map(),
            selectedChatId: null,
            selectedChat: null,
            virtualChatUser: null,
            notifications: [],
            unreadNotificationCount: 0,
            unreadNotificationCountFromSocket: 0,
            unreadMessageCount: 0,
            error: null,
            isLoadingChats: false,
            isLoadingNotifications: false,
          },
          false,
          "clearAllData"
        );
      },

      ensureNotificationsLoaded: () => {
        const { notifications, isLoadingNotifications } = get();
        if (notifications.length === 0 && !isLoadingNotifications) {
          get().fetchNotifications(true).catch(console.error);
        }
      },

      refreshChatList: async () => {
        console.log("🔄 Force refreshing chat list...");
        return get().fetchChatList();
      },
    }),
    { name: "app-store" }
  )
);

export default useAppStore;