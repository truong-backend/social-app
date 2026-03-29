export interface UserProfile {
  id: string
  username: string
  familyName: string
  givenName: string
  bio: string | null
  profilePictureUrl: string | null
  birthdate: string
  friendCount: number
  isFriend: boolean
  isBlocked: boolean
  hasSentRequest: boolean
  hasReceivedRequest: boolean
}

export interface UserSummary {
  id: string
  username: string
  familyName: string
  givenName: string
  profilePictureUrl: string | null
}

export interface ChangeNameRequest {
  familyName: string
  givenName: string
}

export interface ChangeUsernameRequest {
  username: string
}

export interface ChangeBirthdateRequest {
  birthdate: string
}

export interface ChangeBioRequest {
  bio: string
}