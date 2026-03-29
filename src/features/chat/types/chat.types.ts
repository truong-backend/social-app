export interface Chat {
  id: string
  memberIds: string[]
  createdAt: string
}

export interface Message {
  id: string
  senderId: string
  chatId: string
  content: string
  attachedFileUrls: string[]
  isRead: boolean
  sentAt: string
  updatedAt: string
}

export interface SendMessageRequest {
  content: string
}

export interface UpdateMessageRequest {
  content: string
}

export interface DeleteMessageRequest {
  type: 'EVERY' | 'USER_ONLY'
}