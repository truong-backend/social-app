// Components
export { UserAvatar }      from './components/UserAvatar'
export { UserProfileCard } from './components/UserProfileCard'

// Hooks — queries
export { useProfile, useMyProfile } from './hooks/useProfile'
export { useSearchUsers }           from './hooks/useSearchUsers'

// Hooks — mutations
export {
  useChangeName,
  useChangeUsername,
  useChangeBirthdate,
  useChangeBio,
  useUpdateProfilePicture,
} from './hooks/useUpdateProfile'

// Types
export type {
  UserProfile,
  UserSummary,
  ChangeNameRequest,
  ChangeUsernameRequest,
  ChangeBirthdateRequest,
  ChangeBioRequest,
} from './types/user.types'

// Constants
export { USER_QUERY_KEYS } from './constants/user.constants'

// API
export {
  getProfileApi,
  getMyProfileApi,
  searchUsersApi,
  changeNameApi,
  changeUsernameApi,
  changeBirthdateApi,
  changeBioApi,
  updateProfilePictureApi,
} from './api/user.api'