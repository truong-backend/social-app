export type CallStatus =
  | 'idle'         // chưa có cuộc gọi nào
  | 'outgoing'     // đang gọi đi, chờ đối phương bắt
  | 'incoming'     // có cuộc gọi đến, chờ mình bắt
  | 'connected'    // đang trong cuộc gọi
  | 'ended'        // cuộc gọi kết thúc

export interface CallSession {
  callId:        string       // ID từ Stringee API
  messageId:     string       // ID của Message (Call entity)
  chatId:        string
  callerId:      string       // userId người gọi
  callerName:    string
  receiverId:    string       // userId người nhận
  receiverName:  string
  isVideoCall:   boolean
  status:        CallStatus
  startedAt:     string | null
  endedAt:       string | null
}

// Payload WebSocket push khi có cuộc gọi đến
export interface IncomingCallPayload {
  callId:      string
  messageId:   string
  chatId:      string
  callerId:    string
  callerName:  string
  isVideoCall: boolean
}

// Payload WebSocket push khi cuộc gọi kết thúc
export interface CallEndedPayload {
  callId:   string
  chatId:   string
  endedAt:  string
}