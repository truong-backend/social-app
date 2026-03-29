import { useQuery } from '@tanstack/react-query'
import { searchUsersApi } from '../api/user.api'
import { USER_QUERY_KEYS } from '../constants/user.constants'

export const useSearchUsers = (keyword: string) => {
  return useQuery({
    queryKey: USER_QUERY_KEYS.search(keyword),
    queryFn: () => searchUsersApi(keyword),
    enabled: keyword.trim().length >= 2,
    staleTime: 1000 * 30,
  })
}