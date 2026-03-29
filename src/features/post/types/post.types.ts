import type { Privacy } from '@/types/api.types'

export interface Post {
  id: string
  authorId: string
  authorUsername: string | null
  authorProfilePic: string | null
  content: string
  privacy: Privacy
  likeCount: number
  shareCount: number
  commentCount: number
  isLiked: boolean
  isShared: boolean
  sharedFromPostId: string | null
  attachedFileUrls: string[]
  createdAt: string
  updatedAt: string
}

export interface CreatePostRequest {
  content: string
  privacy: Privacy
}

export interface UpdatePostContentRequest {
  content: string
}

export interface UpdatePostPrivacyRequest {
  privacy: Privacy
}

export interface SharePostRequest {
  content: string
  privacy: Privacy
}

export interface PostFeedParams {
  skip?: number
  limit?: number
}