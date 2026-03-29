export const USER_QUERY_KEYS = {
  all: ['users'] as const,
  profile: (userId: string) => [...USER_QUERY_KEYS.all, 'profile', userId] as const,
  myProfile: () => [...USER_QUERY_KEYS.all, 'me'] as const,
  search: (keyword: string) => [...USER_QUERY_KEYS.all, 'search', keyword] as const,
} as const