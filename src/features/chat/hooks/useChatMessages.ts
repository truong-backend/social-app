import { useInfiniteQuery } from '@tanstack/react-query'
import { getChatMessagesApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS, MESSAGE_PAGE_SIZE } from '../constants/chat.constants'

export const useChatMessages = (chatId: string) => {
  return useInfiniteQuery({
    queryKey: CHAT_QUERY_KEYS.messages(chatId),
    queryFn: ({ pageParam }) =>
      getChatMessagesApi(chatId, pageParam as number, MESSAGE_PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < MESSAGE_PAGE_SIZE) return undefined
      return allPages.length * MESSAGE_PAGE_SIZE
    },
    enabled: !!chatId,
    select: (data) => ({
      pages: [...data.pages].reverse(),
      pageParams: data.pageParams,
    }),
  })
}