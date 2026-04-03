// src/features/call/services/stringee.singleton.ts

import type { StringeeClientInstance, StringeeCallInstance } from '../types/stringee.types'

// Module-level singletons — tồn tại suốt lifetime của app
let _client: StringeeClientInstance | null = null
let _call:   StringeeCallInstance   | null = null
let _localVideoEl:  HTMLVideoElement | null = null
let _remoteVideoEl: HTMLVideoElement | null = null

export const StringeeSingleton = {
  getClient:  () => _client,
  setClient:  (c: StringeeClientInstance | null) => { _client = c },

  getCall:    () => _call,
  setCall:    (c: StringeeCallInstance | null)   => { _call = c },

  getLocalVideoEl:  () => _localVideoEl,
  setLocalVideoEl:  (el: HTMLVideoElement | null) => {
    _localVideoEl = el
    // Nếu stream đã có rồi (race condition), attach ngay
    if (el && _pendingLocalStream) {
      el.srcObject = _pendingLocalStream
      _pendingLocalStream = null
    }
  },

  getRemoteVideoEl: () => _remoteVideoEl,
  setRemoteVideoEl: (el: HTMLVideoElement | null) => {
    _remoteVideoEl = el
    if (el && _pendingRemoteStream) {
      el.srcObject = _pendingRemoteStream
      _pendingRemoteStream = null
    }
  },
}

// Buffer stream nếu video element chưa mount kịp
let _pendingLocalStream:  MediaStream | null = null
let _pendingRemoteStream: MediaStream | null = null

export const attachLocalStream = (stream: MediaStream) => {
  const el = _localVideoEl
  if (el) el.srcObject = stream
  else _pendingLocalStream = stream
}

export const attachRemoteStream = (stream: MediaStream) => {
  const el = _remoteVideoEl
  if (el) el.srcObject = stream
  else _pendingRemoteStream = stream
}