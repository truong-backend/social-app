"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import api, { isTokenValid } from "@/utils/axios";
import { 
  getStompClient, 
  subscribe, 
  unsubscribe, 
  isConnected,
  connect 
} from "@/utils/socket";
import useAppStore from "@/store/ZustandStore";

export default function useChat(chatId) {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [totalMessages, setTotalMessages] = useState(0);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');
  const [isTyping, setIsTyping] = useState(false);

  // ✅ FIX: Dùng ref thay vì state cho currentUserId
  // → Không trigger re-render/re-subscription khi userId load xong
  const currentUserIdRef = useRef(null);
  const [currentUserId, setCurrentUserId] = useState(null);

  const typingTimeoutRef = useRef(null);
  const subscriptionRef = useRef(null);
  const subscribedChatIdRef = useRef(null);
  const reconnectIntervalRef = useRef(null);

  // Get userId từ localStorage — chỉ chạy 1 lần
  useEffect(() => {
    const uid = localStorage.getItem("userId");
    if (uid) {
      currentUserIdRef.current = uid;
      setCurrentUserId(uid);
    }
  }, []);

  const updateChatList = useCallback((newMessage) => {
    const { chatList } = useAppStore.getState();
    const foundChat = chatList.find((c) => c.chatId === chatId);

    if (foundChat) {
      const updatedChat = {
        ...foundChat,
        latestMessage: {
          id: newMessage.id,
          content: newMessage.content,
          sentAt: newMessage.sentAt,
          sender: newMessage.sender,
          messageType: newMessage.messageType,
          attachment: newMessage.attachment,
          attachments: newMessage.attachments,
          deleted: newMessage.deleted || false
        },
        updatedAt: newMessage.sentAt,
        notReadMessageCount:
          (foundChat.notReadMessageCount || 0) + (newMessage.isOwnMessage ? 0 : 1),
      };

      const otherChats = chatList.filter((c) => c.chatId !== chatId);
      const newChatList = [...otherChats, updatedChat];

      useAppStore.setState({ 
        chatList: newChatList.map(chat => ({...chat}))
      });
    } else {
      console.warn(`⚠️ Không tìm thấy chat với chatId: ${chatId}`);
    }
  }, [chatId]);

  // ✅ FIX: Dùng ref để đọc currentUserId trong callback
  // → Không cần currentUserId trong dependency array
  const handleTypingNotification = useCallback((data) => {
    if (data.id === currentUserIdRef.current) return;

    if (data.command === "TYPING") {
      setIsTyping(true);
    } else if (data.command === "STOP_TYPING") {
      setIsTyping(false);
    }
  }, []); // ✅ Không còn [currentUserId] trong deps

  const handleBlockNotification = useCallback((data) => {
    const { updateBlockStatus } = useAppStore.getState();

    if (data.command === "HAS_BEEN_BLOCKED") {
      updateBlockStatus(chatId, {
        blockStatus: "HAS_BEEN_BLOCKED",
        blockedAt: data.blockedAt || new Date().toISOString(),
        blockReason: data.blockReason || "Blocked by user"
      });
    } else if (data.command === "HAS_BEEN_UNBLOCKED") {
      updateBlockStatus(chatId, {
        blockStatus: "NORMAL",
        blockedAt: null,
        blockReason: null
      });
    }
  }, [chatId]);

  const handleReadingNotification = useCallback((data) => {
    if (data.id === currentUserIdRef.current) return; // ✅ Dùng ref

    setMessages((prevMessages) =>
      prevMessages.map((message) => ({ ...message, isRead: true }))
    );
  }, []); // ✅ Không còn [currentUserId] trong deps

  // ✅ FIX: handleMessage không còn depend vào currentUserId state
  // → Không tạo lại khi userId load → không trigger re-subscription
  const handleMessage = useCallback((message) => {
    try {
      const data = JSON.parse(message.body);

      if (data.command === "TYPING" || data.command === "STOP_TYPING") {
        handleTypingNotification(data);
        return;
      }

      if (data.command === "HAS_BEEN_BLOCKED" || data.command === "HAS_BEEN_UNBLOCKED") {
        handleBlockNotification(data);
        return;
      }

      if (data.command === "READING") {
        handleReadingNotification(data);
        return;
      }

      if (data.command === "DELETE") {
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === data.id
              ? { ...msg, content: "[Tin nhắn đã bị xóa]", deleted: true }
              : msg
          )
        );
        return;
      }

      if (data.command === "EDIT") {
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === data.id
              ? { ...msg, content: data.message, updated: true, editedAt: data.editedAt || new Date().toISOString() }
              : msg
          )
        );
        return;
      }

      // ✅ Dùng ref để check isOwnMessage, không cần state
      const newMessage = {
        ...data,
        isOwnMessage: data.sender?.id === currentUserIdRef.current,
      };

      if (!newMessage.isOwnMessage) {
        setIsTyping(false);
      }

      setMessages((prev) => [newMessage, ...prev]);

      requestAnimationFrame(() => {
        updateChatList(newMessage);
      });

    } catch (err) {
      console.error("❌ Error parsing message:", err);
    }
  }, [updateChatList, handleTypingNotification, handleBlockNotification, handleReadingNotification]);
  // ✅ currentUserId đã bị xóa khỏi deps → handleMessage ổn định, không tạo lại

  // Load messages lần đầu khi chatId thay đổi
  useEffect(() => {
    if (!chatId) return;

    const fetchInitialMessages = async () => {
      try {
        setLoading(true);
        setMessages([]);
        setHasMore(true);
        setTotalMessages(0);
        setIsTyping(false);

        const limit = 20;
        const skip = 0;

        const res = await api.get(`/v1/chat/messages/${chatId}?skip=${skip}&limit=${limit}`);
        const fetchedMessages = res.data.body || [];

        setMessages(fetchedMessages);
        setTotalMessages(fetchedMessages.length);
        setHasMore(fetchedMessages.length === limit);
      } catch (err) {
        console.error("❌ Lỗi tải tin nhắn:", err);
        setMessages([]);
        setHasMore(false);
      } finally {
        setLoading(false);
      }
    };

    fetchInitialMessages();
  }, [chatId]); // ✅ Bỏ currentUserId khỏi deps — không cần thiết

  // Load more messages (infinity scroll)
  const loadMoreMessages = useCallback(async () => {
    if (!chatId || loadingMore || !hasMore) return;

    try {
      setLoadingMore(true);

      const limit = 20;
      const currentCount = messages.length;
      const skip = currentCount;

      const res = await api.get(`/v1/chat/messages/${chatId}?skip=${skip}&limit=${limit}`);
      const olderMessages = res.data.body || [];

      if (olderMessages.length > 0) {
        setMessages(prev => [...prev, ...olderMessages]);
        setTotalMessages(prev => prev + olderMessages.length);
        setHasMore(olderMessages.length === limit);
      } else {
        setHasMore(false);
      }
    } catch (err) {
      console.error("❌ Lỗi load thêm tin nhắn:", err);
      setHasMore(false);
    } finally {
      setLoadingMore(false);
    }
  }, [chatId, messages.length, loadingMore, hasMore]);

  // ✅ FIX: Dependency array chỉ còn [chatId, handleMessage]
  // handleMessage giờ ổn định → useEffect này chỉ chạy khi đổi chat
  useEffect(() => {
    if (!chatId) return;

    // Cleanup subscription cũ trước
    if (subscriptionRef.current) {
      console.log(`🧹 Unsubscribing from previous chat:${subscribedChatIdRef.current}`);
      unsubscribe(`/chat/${subscribedChatIdRef.current}`);
      subscriptionRef.current = null;
      subscribedChatIdRef.current = null;
    }

    if (reconnectIntervalRef.current) {
      clearInterval(reconnectIntervalRef.current);
      reconnectIntervalRef.current = null;
    }

    const subscribeToChat = async () => {
      try {
        console.log(`🔌 Subscribing to chat:${chatId}...`);
        setConnectionStatus('connecting');

        await getStompClient();

        const subscription = await subscribe(`/chat/${chatId}`, handleMessage);

        if (subscription) {
          subscriptionRef.current = subscription;
          subscribedChatIdRef.current = chatId;
          setConnectionStatus('connected');
          console.log(`✅ Successfully subscribed to chat:${chatId}`);
        } else {
          setConnectionStatus('error');
          console.error(`❌ Failed to subscribe to chat:${chatId}`);
        }
      } catch (error) {
        setConnectionStatus('error');
        console.error(`❌ Error subscribing to chat:${chatId}:`, error);
      }
    };

    subscribeToChat();

    reconnectIntervalRef.current = setInterval(async () => {
      const connected = isConnected();

      if (!connected && isTokenValid()) {
        console.log(`🔁 Reconnecting to chat:${chatId}...`);
        setConnectionStatus('reconnecting');

        try {
          await connect();

          if (isConnected()) {
            const subscription = await subscribe(`/chat/${chatId}`, handleMessage);
            if (subscription) {
              subscriptionRef.current = subscription;
              subscribedChatIdRef.current = chatId;
              setConnectionStatus('connected');
              console.log(`✅ Reconnected and resubscribed to chat:${chatId}`);
            }
          }
        } catch (error) {
          setConnectionStatus('error');
          console.error(`❌ Reconnection failed for chat:${chatId}:`, error);
        }
      } else {
        setConnectionStatus(connected ? 'connected' : 'disconnected');
      }
    }, 15000);

    return () => {
      console.log(`🧹 Cleaning up chat:${chatId} subscription...`);

      if (subscriptionRef.current && subscribedChatIdRef.current === chatId) {
        unsubscribe(`/chat/${chatId}`);
        subscriptionRef.current = null;
        subscribedChatIdRef.current = null;
      }

      if (reconnectIntervalRef.current) {
        clearInterval(reconnectIntervalRef.current);
        reconnectIntervalRef.current = null;
      }

      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = null;
      }

      setConnectionStatus('disconnected');
    };
  }, [chatId, handleMessage]); // ✅ Bỏ currentUserId — chỉ còn chatId và handleMessage ổn định

  // Cleanup khi component unmount
  useEffect(() => {
    return () => {
      if (subscriptionRef.current && subscribedChatIdRef.current) {
        console.log(`🧹 Component unmounting, cleaning up chat:${subscribedChatIdRef.current}`);
        unsubscribe(`/chat/${subscribedChatIdRef.current}`);
      }

      if (reconnectIntervalRef.current) {
        clearInterval(reconnectIntervalRef.current);
      }

      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
    };
  }, []);

  const forceReconnect = useCallback(async () => {
    if (!chatId) return;

    console.log(`🔄 Force reconnecting to chat:${chatId}...`);
    setConnectionStatus('connecting');

    try {
      if (subscriptionRef.current) {
        unsubscribe(`/chat/${chatId}`);
        subscriptionRef.current = null;
        subscribedChatIdRef.current = null;
      }

      await connect();

      const subscription = await subscribe(`/chat/${chatId}`, handleMessage);
      if (subscription) {
        subscriptionRef.current = subscription;
        subscribedChatIdRef.current = chatId;
        setConnectionStatus('connected');
        console.log(`✅ Force reconnected to chat:${chatId}`);
      } else {
        setConnectionStatus('error');
      }
    } catch (error) {
      setConnectionStatus('error');
      console.error(`❌ Force reconnect failed:`, error);
    }
  }, [chatId, handleMessage]);

  return {
    messages,
    loading,
    loadingMore,
    hasMore,
    totalMessages,
    currentUserId,
    connectionStatus,
    loadMoreMessages,
    forceReconnect,
    isTyping,
  };
}