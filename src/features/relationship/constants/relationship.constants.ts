export const RELATIONSHIP_QUERY_KEYS = {
  friends:   ['relationships', 'friends']            as const,
  received:  ['relationships', 'requests', 'received'] as const,
  sent:      ['relationships', 'requests', 'sent']   as const,
  blocked:   ['relationships', 'blocked']            as const,
} as const