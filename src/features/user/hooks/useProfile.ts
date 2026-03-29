import { useQuery } from '@tanstack/react-query'
import { getProfileApi, getMyProfileApi } from '../api/user.api'
import { USER_QUERY_KEYS } from '../constants/user.constants'

export const useProfile = (userId: string) => {
  return useQuery({
    queryKey: USER_QUERY_KEYS.profile(userId),
    queryFn: () => getProfileApi(userId),
    enabled: !!userId,
  })
}

export const useMyProfile = () => {
  return useQuery({
    queryKey: USER_QUERY_KEYS.myProfile(),
    queryFn: getMyProfileApi,
  })
}