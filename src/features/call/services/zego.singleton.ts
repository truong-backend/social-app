// src/features/call/services/zego.singleton.ts

import type { ZegoEngineInstance } from '../types/zego.types'

let _engine: ZegoEngineInstance | null = null
let _localStream:  MediaStream | null = null
let _remoteStream: MediaStream | null = null
let _localVideoEl:  HTMLVideoElement | null = null
let _remoteVideoEl: HTMLVideoElement | null = null

export const ZegoSingleton = {
  getEngine: () => _engine,
  setEngine: (e: ZegoEngineInstance | null) => { _engine = e },

  getLocalStream: () => _localStream,
  setLocalStream: (s: MediaStream | null) => { _localStream = s },

  getRemoteStream: () => _remoteStream,
  setRemoteStream: (s: MediaStream | null) => { _remoteStream = s },

  getLocalVideoEl:  () => _localVideoEl,
  setLocalVideoEl:  (el: HTMLVideoElement | null) => {
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

let _pendingLocalStream:  MediaStream | null = null
let _pendingRemoteStream: MediaStream | null = null

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
