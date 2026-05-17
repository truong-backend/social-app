"use client";

import React, {useEffect, useRef, useState} from "react";
import {useCall} from "@/context/CallContext";

const CallVideo = ({onCallEnd}) => {
    const localVideoRef = useRef(null);
    const remoteVideoRef = useRef(null);
    const {
        endCall,
        toggleMute,
        toggleLocalVideo,
        callStatus,
        currentCall,
        isCallEnding,
        cleanupCall,
        localStream,
        remoteStream,
        mediaPermissions
    } = useCall();

    const [isCameraOn, setIsCameraOn] = useState(true);
    const [isMicOn, setIsMicOn] = useState(true);
    const [autoplayError, setAutoplayError] = useState(false);

    useEffect(() => {
        console.log("[DEBUG] CallVideo mount/update:");
        console.log("  callStatus:", callStatus);
        console.log("  currentCall:", !!currentCall);
        console.log("  isCallEnding:", isCallEnding);
        console.log("  localStream:", !!localStream);
        console.log("  remoteStream:", !!remoteStream);
    }, [callStatus, currentCall, isCallEnding, localStream, remoteStream]);

    // Sync button state với actual stream tracks
    useEffect(() => {
        if (localStream) {
            const videoTracks = localStream.getVideoTracks();
            const audioTracks = localStream.getAudioTracks();
            setIsCameraOn(videoTracks.length > 0 && videoTracks[0].enabled);
            setIsMicOn(audioTracks.length > 0 && audioTracks[0].enabled);
        }
    }, [localStream]);

    // Setup local video
    useEffect(() => {
        if (localStream && localVideoRef.current) {
            console.log("[DEBUG] Assigning local stream to video element");
            localVideoRef.current.srcObject = localStream;
            localVideoRef.current.play().catch(error => {
                console.warn("[DEBUG] Local video autoplay failed:", error);
            });
        }
    }, [localStream]);

    // ✅ FIX: Setup remote video với retry loop
    // Nếu remoteVideoRef chưa mount khi stream arrive → thử lại mỗi 100ms tối đa 3 giây
    useEffect(() => {
        if (!remoteStream) return;

        let retryCount = 0;
        const maxRetries = 30; // 30 * 100ms = 3 giây
        let timerId = null;

        const assignRemoteStream = () => {
            if (remoteVideoRef.current) {
                console.log("[DEBUG] ✅ Assigning remote stream, attempt:", retryCount + 1);
                remoteVideoRef.current.srcObject = remoteStream;
                remoteVideoRef.current.play().catch(error => {
                    console.warn("[DEBUG] Remote video autoplay failed:", error);
                    setAutoplayError(true);
                });
            } else {
                retryCount++;
                if (retryCount < maxRetries) {
                    console.warn("[DEBUG] remoteVideoRef not ready, retry:", retryCount);
                    timerId = setTimeout(assignRemoteStream, 100);
                } else {
                    console.error("[DEBUG] ❌ remoteVideoRef never ready after", maxRetries, "attempts");
                }
            }
        };

        assignRemoteStream();

        // cleanup nếu remoteStream thay đổi trước khi retry xong
        return () => {
            if (timerId) clearTimeout(timerId);
        };
    }, [remoteStream]);

    const toggleCamera = () => {
        if (!mediaPermissions.video || !localStream) return;
        const videoTracks = localStream.getVideoTracks();
        if (videoTracks.length === 0) return;
        const newState = !isCameraOn;
        toggleLocalVideo(!newState);
        setIsCameraOn(newState);
    };

    const toggleMicrophone = () => {
        if (!mediaPermissions.audio || !localStream) return;
        const audioTracks = localStream.getAudioTracks();
        if (audioTracks.length === 0) return;
        const newState = !isMicOn;
        toggleMute(!newState);
        setIsMicOn(newState);
    };

    const handleEndCall = () => {
        console.log("[DEBUG] End call clicked");
        endCall();
    };

    const handleClose = () => {
        console.log("[DEBUG] Close clicked");
        cleanupCall(11);
        if (onCallEnd) onCallEnd();
    };

    if (!currentCall && !isCallEnding) {
        return null;
    }

    return (
        <div className="fixed inset-0 bg-black z-[999] flex items-center justify-center">
            {isCallEnding ? (
                /* ── Màn hình kết thúc ── */
                <div className="text-center text-white space-y-4">
                    <div className="text-6xl mb-4">📞</div>
                    <p className="text-2xl font-semibold">Cuộc gọi đã kết thúc</p>
                    <p className="text-lg text-gray-300">Đang đóng...</p>
                    <button
                        onClick={handleClose}
                        className="bg-blue-600 px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium"
                    >
                        Đóng ngay
                    </button>
                </div>
            ) : (
                <>
                    {/* Status bar */}
                    <div className="absolute top-4 left-1/2 -translate-x-1/2 z-20">
                        <div className="bg-black/60 px-4 py-2 rounded-full text-white text-sm">
                            {callStatus}
                        </div>
                    </div>

                    {/* ── Remote video — toàn màn hình ── */}
                    <div className="absolute inset-0 z-[1]">
                        {remoteStream ? (
                            <video
                                ref={remoteVideoRef}
                                autoPlay
                                playsInline
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center bg-gray-800">
                                <div className="text-white text-center">
                                    <div className="text-6xl mb-4">👤</div>
                                    <p className="text-xl">Đang chờ video từ đối phương...</p>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* ── Local video — góc phải dưới ── */}
                    <div className="absolute bottom-28 right-4 w-32 h-48 md:w-48 md:h-64 bg-gray-800 rounded-xl overflow-hidden border-2 border-white z-10 shadow-xl">
                        {localStream && isCameraOn ? (
                            <video
                                ref={localVideoRef}
                                autoPlay
                                playsInline
                                muted
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <div className="w-full h-full bg-gray-900 flex items-center justify-center">
                                <div className="text-white text-center text-xs px-2">
                                    <div className="text-3xl mb-1">📷</div>
                                    <p>{!localStream ? "Đang khởi tạo..." : "Camera tắt"}</p>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* ── Controls ── */}
                    <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex items-center space-x-4 z-20">
                        {/* Camera toggle */}
                        <button
                            onClick={toggleCamera}
                            disabled={!mediaPermissions.video || !localStream}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isCameraOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${(!mediaPermissions.video || !localStream) ? "opacity-40 cursor-not-allowed" : ""}`}
                            title={isCameraOn ? "Tắt camera" : "Bật camera"}
                        >
                            <span className="text-lg">{isCameraOn ? "📹" : "📷"}</span>
                        </button>

                        {/* Mic toggle */}
                        <button
                            onClick={toggleMicrophone}
                            disabled={!mediaPermissions.audio || !localStream}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isMicOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${(!mediaPermissions.audio || !localStream) ? "opacity-40 cursor-not-allowed" : ""}`}
                            title={isMicOn ? "Tắt mic" : "Bật mic"}
                        >
                            <span className="text-lg">{isMicOn ? "🎤" : "🔇"}</span>
                        </button>

                        {/* End call */}
                        <button
                            onClick={handleEndCall}
                            className="flex items-center space-x-2 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-full shadow-lg transition-colors font-medium"
                        >
                            <span>📞</span>
                            <span>Kết thúc</span>
                        </button>
                    </div>

                    {/* ── Indicators góc trên phải ── */}
                    <div className="absolute top-4 right-4 flex flex-col space-y-2 z-20">
                        {!isCameraOn && (
                            <div className="bg-red-600/80 px-3 py-1 rounded-full text-white text-xs flex items-center space-x-1">
                                <span>📷</span><span>Camera tắt</span>
                            </div>
                        )}
                        {!isMicOn && (
                            <div className="bg-red-600/80 px-3 py-1 rounded-full text-white text-xs flex items-center space-x-1">
                                <span>🔇</span><span>Mic tắt</span>
                            </div>
                        )}
                        {autoplayError && (
                            <div className="bg-yellow-600/80 px-3 py-1 rounded-full text-white text-xs flex items-center space-x-1">
                                <span>⚠️</span>
                                <button
                                    onClick={() => {
                                        localVideoRef.current?.play();
                                        remoteVideoRef.current?.play();
                                        setAutoplayError(false);
                                    }}
                                    className="underline"
                                >
                                    Bấm để phát video
                                </button>
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
};

export default CallVideo;