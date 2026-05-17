"use client";

import React, { useEffect, useRef, useState } from "react";
import { useCall } from "@/context/CallContext";

const CallVideo = ({ onCallEnd }) => {
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
        mediaPermissions,
    } = useCall();

    const [isCameraOn, setIsCameraOn] = useState(true);
    const [isMicOn, setIsMicOn] = useState(true);
    const [autoplayError, setAutoplayError] = useState(false);

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
            localVideoRef.current.srcObject = localStream;
            localVideoRef.current.play().catch(() => {});
        }
    }, [localStream]);

    // ✅ FIX CHÍNH: remoteVideoRef luôn được mount (không conditional render)
    // nên ref luôn sẵn sàng khi remoteStream arrive — không cần retry
    useEffect(() => {
        if (!remoteStream) return;
        if (!remoteVideoRef.current) {
            console.error("[DEBUG] ❌ remoteVideoRef is null — không thể gán stream");
            return;
        }
        console.log("[DEBUG] ✅ Gán remote stream vào video element");
        remoteVideoRef.current.srcObject = remoteStream;
        remoteVideoRef.current.play().catch((err) => {
            console.warn("[DEBUG] Remote autoplay failed:", err);
            setAutoplayError(true);
        });
    }, [remoteStream]);

    const toggleCamera = () => {
        if (!mediaPermissions.video || !localStream) return;
        if (localStream.getVideoTracks().length === 0) return;
        const newState = !isCameraOn;
        toggleLocalVideo(!newState);
        setIsCameraOn(newState);
    };

    const toggleMicrophone = () => {
        if (!mediaPermissions.audio || !localStream) return;
        if (localStream.getAudioTracks().length === 0) return;
        const newState = !isMicOn;
        toggleMute(!newState);
        setIsMicOn(newState);
    };

    const handleEndCall = () => endCall();

    const handleClose = () => {
        cleanupCall(11);
        if (onCallEnd) onCallEnd();
    };

    if (!currentCall && !isCallEnding) return null;

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
                    {/* Status */}
                    <div className="absolute top-4 left-1/2 -translate-x-1/2 z-20 pointer-events-none">
                        <div className="bg-black/60 px-4 py-2 rounded-full text-white text-sm">
                            {callStatus}
                        </div>
                    </div>

                    {/* ── Remote video — toàn màn hình ── */}
                    {/* ✅ FIX: video tag LUÔN render (không dùng conditional)
                        để remoteVideoRef.current không bao giờ null khi stream arrive.
                        Dùng CSS để ẩn/hiện thay vì unmount/mount */}
                    <div className="absolute inset-0 z-[1]">
                        {/* Placeholder khi chưa có remote stream */}
                        {!remoteStream && (
                            <div className="absolute inset-0 flex items-center justify-center bg-gray-800">
                                <div className="text-white text-center">
                                    <div className="text-6xl mb-4">👤</div>
                                    <p className="text-xl">Đang chờ video từ đối phương...</p>
                                </div>
                            </div>
                        )}
                        {/* Video element LUÔN mount, ẩn bằng visibility khi chưa có stream */}
                        <video
                            ref={remoteVideoRef}
                            autoPlay
                            playsInline
                            className="w-full h-full object-cover"
                            style={{ visibility: remoteStream ? "visible" : "hidden" }}
                        />
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
                        <button
                            onClick={toggleCamera}
                            disabled={!mediaPermissions.video || !localStream}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isCameraOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${(!mediaPermissions.video || !localStream) ? "opacity-40 cursor-not-allowed" : ""}`}
                        >
                            <span className="text-lg">{isCameraOn ? "📹" : "📷"}</span>
                        </button>

                        <button
                            onClick={toggleMicrophone}
                            disabled={!mediaPermissions.audio || !localStream}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isMicOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${(!mediaPermissions.audio || !localStream) ? "opacity-40 cursor-not-allowed" : ""}`}
                        >
                            <span className="text-lg">{isMicOn ? "🎤" : "🔇"}</span>
                        </button>

                        <button
                            onClick={handleEndCall}
                            className="flex items-center space-x-2 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-full shadow-lg transition-colors font-medium"
                        >
                            <span>📞</span>
                            <span>Kết thúc</span>
                        </button>
                    </div>

                    {/* ── Indicators ── */}
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