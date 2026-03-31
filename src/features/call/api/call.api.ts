import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'

export interface InitiateCallResponse {
  callId:    string    // Stringee callId
  messageId: string    // Message entity id trong Neo4j
  chatId:    string
}

/**
 * Gọi BE để tạo Call entity và lấy callId từ Stringee.
 * BE sẽ push IncomingCallPayload đến receiver qua WebSocket.
 */
export const initiateCallApi = async (
  targetUserId: string,
  isVideoCall: boolean,
): Promise<InitiateCallResponse> => {
  const response = await axiosInstance.post('/api/messages/calls', {
    targetUserId,
    isVideoCall,
  })
  return unwrapData(response)
}

/**
 * Thông báo BE người nhận đã bắt máy.
 */
export const answerCallApi = async (callId: string): Promise<void> => {
  await axiosInstance.post(`/api/messages/calls/${callId}/answer`)
}

/**
 * Thông báo BE kết thúc cuộc gọi (cả 2 phía đều có thể gọi).
 */
export const endCallApi = async (callId: string): Promise<void> => {
  await axiosInstance.post(`/api/messages/calls/${callId}/end`)
}

/**
 * Lấy Stringee access token từ BE.
 * BE dùng Stringee REST API để tạo token với userId.
 */
export const getStringeeTokenApi = async (): Promise<string> => {
  const response = await axiosInstance.get('/api/messages/calls/stringee-token')
  // return unwrapData<string>(response) ?? ''
  return response.data
}