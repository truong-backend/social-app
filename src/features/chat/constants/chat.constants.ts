export const CHAT_QUERY_KEYS = {
  all: ['chats'] as const,
  list: () => [...CHAT_QUERY_KEYS.all, 'list'] as const,
  messages: (chatId: string) => [...CHAT_QUERY_KEYS.all, 'messages', chatId] as const,
  search: (query: string) => [...CHAT_QUERY_KEYS.all, 'search', query] as const,
} as const

export const MESSAGE_PAGE_SIZE = 20

export const CHAT_WEBSOCKET_EVENTS = {
  NEW_MESSAGE: 'new_message',
  MESSAGE_UPDATED: 'message_updated',
  MESSAGE_DELETED: 'message_deleted',
} as const