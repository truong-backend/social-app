export const CALL_WEBSOCKET_EVENTS = {
  INCOMING_CALL: 'incoming_call',
  CALL_ENDED:    'call_ended',
  CALL_ACCEPTED: 'call_accepted',
} as const

export const CALL_RING_TIMEOUT_MS    = 40_000   // 40s không bắt → tự huỷ
export const CALL_CONNECT_TIMEOUT_MS = 15_000   // 15s không kết nối được → báo lỗi

// ZegoCloud SDK được load qua CDN script tag trong index.html
// window.ZegoExpressEngine sẽ có sẵn sau khi SDK load
export const ZEGO_SDK_URL = 'https://unpkg.com/zego-express-engine-webrtc@latest/index.js'
