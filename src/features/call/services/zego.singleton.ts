// src/features/call/services/zego.singleton.ts
// Singleton lưu ZegoExpressEngine instance xuyên suốt app

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let _engine: any = null
let _localStream:  MediaStream | null = null
let _remoteStream: MediaStream | null = null
let _localVideoEl:  HTMLVideoElement | null = null
let _remoteVideoEl: HTMLVideoElement | null = null
let _pendingLocalStream:  MediaStream | null = null
let _pendingRemoteStream: MediaStream | null = null

export const ZegoSingleton = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getEngine: (): any => _engine,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setEngine: (e: any) => { _engine = e },

  getLocalStream: () => _localStream,
  setLocalStream: (s: MediaStream | null) => { _localStream = s },

  getRemoteStream: () => _remoteStream,
  setRemoteStream: (s: MediaStream | null) => { _remoteStream = s },

  getLocalVideoEl: () => _localVideoEl,
  setLocalVideoEl: (el: HTMLVideoElement | null) => {
    _localVideoEl = el
    if (el && _pendingLocalStream) {
      el.srcObject = _pendingLocalStream
      el.play().catch(() => {})
      _pendingLocalStream = null
    }
  },

  getRemoteVideoEl: () => _remoteVideoEl,
  setRemoteVideoEl: (el: HTMLVideoElement | null) => {
    _remoteVideoEl = el
    if (el && _pendingRemoteStream) {
      el.srcObject = _pendingRemoteStream
      el.play().catch(() => {})
      _pendingRemoteStream = null
    }
  },
}

export const attachLocalStream = (stream: MediaStream) => {
  _localStream = stream
  const el = _localVideoEl
  if (el) {
    el.srcObject = stream
    el.play().catch(() => {})
  } else {
    _pendingLocalStream = stream
  }
}

export const attachRemoteStream = (stream: MediaStream) => {
  _remoteStream = stream
  const el = _remoteVideoEl
  if (el) {
    el.srcObject = stream
    el.play().catch(() => {})
  } else {
    _pendingRemoteStream = stream
  }
}
