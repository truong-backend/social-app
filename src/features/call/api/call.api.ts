import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'

/**
 * BE trả về callId tạm thời ngay lúc initiate.
 */
export interface InitiateCallResponse {
  callId: string
}

/**
 * Gọi BE để khởi tạo cuộc gọi.
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
 * Thông báo BE kết thúc cuộc gọi.
 * Endpoint: POST /api/calls/{callId}/end
 */
export const endCallApi = async (callId: string, targetUserId?: string): Promise<void> => {
  await axiosInstance.post(`/api/calls/${callId}/end`, { targetUserId })
}

/**
 * Lấy Stringee access token từ BE.
 * Endpoint: GET /api/calls/stringee-token
 *
 * BE trả về ApiResponse<{ token: string }>.
 * unwrapData() → { token: "eyJ..." }  (object, không phải string)
 * Phải lấy .token ra → truyền string thuần cho Stringee SDK.
 * Nếu truyền object, SDK encode thành access_token[token]=... → Stringee 500.
 */
export const getStringeeTokenApi = async (): Promise<string> => {
  const response = await axiosInstance.get('/api/calls/stringee-token')
  const data = unwrapData<{ token: string }>(response)
  return data?.token ?? ''
}
