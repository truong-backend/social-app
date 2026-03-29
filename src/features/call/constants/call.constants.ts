export const CALL_WEBSOCKET_EVENTS = {
  INCOMING_CALL: 'incoming_call',
  CALL_ENDED:    'call_ended',
  CALL_ACCEPTED: 'call_accepted',
} as const

export const CALL_RING_TIMEOUT_MS   = 40_000   // 40s không bắt → tự huỷ
export const CALL_CONNECT_TIMEOUT_MS = 15_000   // 15s không kết nối được Stringee → báo lỗi

// Stringee SDK được load qua CDN script tag trong index.html
// Đây chỉ là type reference cho window.StringeeClient
export const STRINGEE_SDK_URL = 'https://cdn.stringee.com/sdk/web/latest/stringee-web-sdk.min.js'