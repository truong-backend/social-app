import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'

/**
 * BE trả về callId tạm thời ngay lúc initiate.
 */
export interface InitiateCallResponse {
  callId: string
}

/**
 * ZegoCloud token + appId trả về từ BE.
 */
export interface ZegoTokenData {
  token: string
  appId: number
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
 * FIX Bug 3: Callee từ chối cuộc gọi.
 * Endpoint: POST /api/calls/{callId}/reject?callerUserId=xxx
 * BE dùng RejectCallUseCase — push call_ended WS về caller.
 *
 * KHÔNG dùng endCallApi (/end) vì endpoint và use case khác nhau.
 */
export const rejectCallApi = async (callId: string, callerUserId: string): Promise<void> => {
  await axiosInstance.post(`/api/calls/${callId}/reject`, null, {
    params: { callerUserId },
  })
}

/**
 * Lấy ZegoCloud token từ BE.
 * Endpoint: GET /api/calls/zego-token
 *
 * BE trả về ApiResponse<{ token: string }>.
 * Token dùng để loginRoom trong ZegoExpressEngine.
 * appId được đọc từ VITE_ZEGOCLOUD_APP_ID env.
 */
export const getZegoTokenApi = async (): Promise<ZegoTokenData> => {
  const response = await axiosInstance.get('/api/calls/zego-token')
  const data = unwrapData<{ token: string }>(response)
  const appId = Number(import.meta.env.VITE_ZEGOCLOUD_APP_ID ?? 0)
  return { token: data?.token ?? '', appId }
}