// src/features/call/types/zego.types.ts
// Wrapper types cho ZegoExpressEngine (npm: zego-express-engine-webrtc)

export interface ZegoRoomConfig {
  userID:    string
  userName:  string
  roomID:    string
  token:     string
  isVideoCall: boolean
}

// Simplified interface - ZegoExpressEngine từ npm đã có đầy đủ types
// ZegoSingleton lưu instance thực (ZegoExpressEngine) dưới dạng unknown
export type ZegoEngineInstance = unknown
