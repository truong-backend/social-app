export type NotificationAction =
  | 'POST'
  | 'BE_FRIEND'
  | 'SENT_ADD_FRIEND_REQUEST'
  | 'LIKED_POST'
  | 'COMMENTED_POST'
  | 'LIKED_COMMENT'
  | 'REPLIED_COMMENT'

export type NotificationTargetType = 'REQUEST' | 'FRIEND' | 'POST' | 'COMMENT'

export interface Notification {
  id: string
  byUserId: string
  action: NotificationAction
  targetType: NotificationTargetType
  targetId: string
  isRead: boolean
  sentAt: string
}