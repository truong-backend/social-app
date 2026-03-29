import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  changeNameApi,
  changeUsernameApi,
  changeBirthdateApi,
  changeBioApi,
  updateProfilePictureApi,
} from '../api/user.api'
import { USER_QUERY_KEYS } from '../constants/user.constants'
import { extractErrorMessage } from '@utils/api-response'

const useInvalidateMyProfile = () => {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: USER_QUERY_KEYS.myProfile() })
}

export const useChangeName = () => {
  const invalidate = useInvalidateMyProfile()
  return useMutation({
    mutationFn: changeNameApi,
    onSuccess: () => { toast.success('Đã cập nhật tên'); invalidate() },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}

export const useChangeUsername = () => {
  const invalidate = useInvalidateMyProfile()
  return useMutation({
    mutationFn: changeUsernameApi,
    onSuccess: () => { toast.success('Đã cập nhật username'); invalidate() },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}

export const useChangeBirthdate = () => {
  const invalidate = useInvalidateMyProfile()
  return useMutation({
    mutationFn: changeBirthdateApi,
    onSuccess: () => { toast.success('Đã cập nhật ngày sinh'); invalidate() },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}

export const useChangeBio = () => {
  const invalidate = useInvalidateMyProfile()
  return useMutation({
    mutationFn: changeBioApi,
    onSuccess: () => { toast.success('Đã cập nhật tiểu sử'); invalidate() },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}

export const useUpdateProfilePicture = () => {
  const invalidate = useInvalidateMyProfile()
  return useMutation({
    mutationFn: (file: File) => updateProfilePictureApi(file),
    onSuccess: () => { toast.success('Đã cập nhật ảnh đại diện'); invalidate() },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })
}