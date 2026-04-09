import { useQuery } from '@tanstack/react-query'
import { searchUsersApi } from '../api/user.api'
import { USER_QUERY_KEYS } from '../constants/user.constants'

export const useSearchUsers = (keyword: string) => {
  const trimmed = keyword.trim()
  return useQuery({
    queryKey: USER_QUERY_KEYS.search(trimmed),
    queryFn: () => searchUsersApi(trimmed),
    enabled: trimmed.length >= 2,
    staleTime: 1000 * 30,
  })
}