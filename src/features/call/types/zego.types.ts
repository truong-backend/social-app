// src/features/call/types/zego.types.ts

export interface ZegoRoomConfig {
  userID:    string
  userName:  string
  roomID:    string
  token:     string
  isVideoCall: boolean
}

export interface ZegoLocalStream {
  audio: boolean
  video: boolean
}

export interface ZegoEngineInstance {
  loginRoom: (
    roomID: string,
    token: string,
    userInfo: { userID: string; userName: string },
    config?: { maxMemberCount?: number }
  ) => Promise<boolean>
  logoutRoom: (roomID?: string) => Promise<void>
  createStream: (config?: { camera?: { audio?: boolean; video?: boolean } }) => Promise<MediaStream>
  destroyStream: (stream: MediaStream) => void
  startPublishingStream: (streamID: string, stream: MediaStream) => void
  stopPublishingStream: (streamID?: string) => void
  startPlayingStream: (streamID: string, config?: object) => Promise<MediaStream>
  stopPlayingStream: (streamID: string) => void
  mutePublishStreamAudio: (streamID: string, mute: boolean) => void
  mutePublishStreamVideo: (streamID: string, mute: boolean) => void
  on: (event: string, handler: (...args: unknown[]) => void) => void
  off: (event: string, handler?: (...args: unknown[]) => void) => void
}
