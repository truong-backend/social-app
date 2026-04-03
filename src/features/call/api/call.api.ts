import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'

/**
 * BE trả về callId tạm thời ngay lúc initiate.
 * messageId và chatId chưa tồn tại lúc này — chúng được tạo
 * bởi HandleStringeeEventUseCase khi Stringee báo "started".
 */
export interface InitiateCallResponse {
  callId: string
}

/**
 * Gọi BE để khởi tạo cuộc gọi.
 * BE push incoming_call đến callee qua WebSocket.
 * Endpoint: POST /api/calls
 */
export const initiateCallApi = async (
  targetUserId: string,
  isVideoCall: boolean,
): Promise<InitiateCallResponse> => {
  const response = await axiosInstance.post('/api/calls', {
    targetUserId,
    isVideoCall,
  })
  return unwrapData(response)
}

/**
 * Thông báo BE kết thúc cuộc gọi (cả 2 phía đều có thể gọi).
 * Endpoint: POST /api/calls/{callId}/end
 */
export const endCallApi = async (callId: string, targetUserId?: string): Promise<void> => {
  await axiosInstance.post(`/api/calls/${callId}/end`, { targetUserId })
}

/**
 * Lấy Stringee access token từ BE.
 * Endpoint: GET /api/calls/stringee-token
 */
export const getStringeeTokenApi = async (): Promise<string> => {
  const response = await axiosInstance.get('/api/calls/stringee-token')
  return unwrapData<string>(response) ?? ''
}