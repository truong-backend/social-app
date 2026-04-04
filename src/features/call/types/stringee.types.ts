// src/features/call/types/stringee.types.ts

export interface StringeeClientInstance {
  connect:    (token: string) => void
  disconnect: () => void
  on:         (event: string, handler: (...args: unknown[]) => void) => void
}

export interface StringeeCallInstance {
  makeCall:    (cb: (result: { r: number; message: string; callId: string }) => void) => void
  answer:      (cb: (result: { r: number }) => void) => void
  hangup:      (cb: (result: { r: number }) => void) => void
  mute:        (muted: boolean) => void
  enableVideo: (enabled: boolean) => void
  on:          (event: string, handler: (...args: unknown[]) => void) => void
  isVideoCall: boolean
  // Fields available on incomingcall event object from Stringee SDK
  fromNumber:  string   // userId của caller
  toNumber:    string   // userId của callee
  callId:      string   // Stringee callId thực
}
