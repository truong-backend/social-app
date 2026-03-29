export const POST_QUERY_KEYS = {
  all: ['posts'] as const,
  feed: () => [...POST_QUERY_KEYS.all, 'feed'] as const,
  detail: (postId: string) => [...POST_QUERY_KEYS.all, 'detail', postId] as const,
  byAuthor: (authorId: string) => [...POST_QUERY_KEYS.all, 'author', authorId] as const,
  search: (keyword: string) => [...POST_QUERY_KEYS.all, 'search', keyword] as const,
} as const

export const POST_PRIVACY_LABELS: Record<string, string> = {
  PUBLIC: 'Công khai',
  FRIENDS: 'Bạn bè',
  PRIVATE: 'Chỉ mình tôi',
}

export const POST_MAX_FILE_SIZE_MB = 50
export const POST_MAX_FILES = 10
export const FEED_PAGE_SIZE = 10