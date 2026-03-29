import { useQuery } from '@tanstack/react-query'
import { getChatListApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS } from '../constants/chat.constants'

export const useChatList = () => {
  return useQuery({
    queryKey: CHAT_QUERY_KEYS.list(),
    queryFn: getChatListApi,
  })
}