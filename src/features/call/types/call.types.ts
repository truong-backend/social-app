export type CallStatus =
  | 'idle'         // chưa có cuộc gọi nào
  | 'outgoing'     // đang gọi đi, chờ đối phương bắt
  | 'incoming'     // có cuộc gọi đến, chờ mình bắt
  | 'connected'    // đang trong cuộc gọi
  | 'ended'        // cuộc gọi kết thúc

export interface CallSession {
  callId:        string       // ID tạm từ BE (call-UUID), Stringee sẽ có callId thực
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
// Khớp với backend IncomingCallPayload record
export interface IncomingCallPayload {
  callId:      string
  callerId:    string
  callerName:  string
  isVideoCall: boolean
}

// Payload WebSocket push khi cuộc gọi kết thúc
// Khớp với backend CallEndedPayload record
export interface CallEndedPayload {
  callId: string
}