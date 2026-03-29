export interface Comment {
  id: string
  authorId: string
  authorUsername: string | null
  authorProfilePic: string | null
  postId: string
  repliedToCommentId: string | null
  content: string
  attachedFileUrls: string[]
  likeCount: number
  replyCount: number
  isLiked: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateCommentRequest {
  content: string
}

export interface UpdateCommentRequest {
  content: string
}

export interface CommentListParams {
  skip?: number
  limit?: number
}