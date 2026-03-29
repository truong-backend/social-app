export const COMMENT_QUERY_KEYS = {
  all: ['comments'] as const,
  byPost: (postId: string) => [...COMMENT_QUERY_KEYS.all, 'post', postId] as const,
  replies: (commentId: string) => [...COMMENT_QUERY_KEYS.all, 'replies', commentId] as const,
} as const

export const COMMENT_PAGE_SIZE = 10