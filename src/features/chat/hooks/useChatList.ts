import { useQuery } from '@tanstack/react-query'
import { getChatListApi } from '../api/chat.api'
import { CHAT_QUERY_KEYS } from '../constants/chat.constants'

export const useChatList = () => {
  return useQuery({
    queryKey: CHAT_QUERY_KEYS.list(),
    queryFn: getChatListApi,
    // Luôn refetch khi focus lại tab để danh sách không bị cũ
    refetchOnWindowFocus: true,
    // staleTime = 0 → mỗi lần invalidate là fetch lại ngay
    staleTime: 0,
  })
}