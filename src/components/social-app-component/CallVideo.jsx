"use client";

import React, { useEffect, useRef, useState } from "react";
import { useCall } from "@/context/CallContext";

const CallVideo = ({ onCallEnd }) => {
    const {
        endCall,
        toggleMute,
        toggleLocalVideo,
        callStatus,
        currentCall,
        isCallEnding,
        cleanupCall,
        mediaPermissions,
        // ✅ KEY: nhận refs từ Context thay vì tạo local refs
        // Refs này được Context dùng để gán stream trực tiếp vào DOM
        remoteVideoElRef,
        localVideoElRef,
        localStreamRef,
    } = useCall();

    const [isCameraOn, setIsCameraOn]   = useState(true);
    const [isMicOn, setIsMicOn]         = useState(true);

    // ✅ KEY FIX: Đăng ký video DOM elements vào Context refs
    // khi component mount — Context sẽ gán stream vào đây
    const localVideoDomRef  = useRef(null);
    const remoteVideoDomRef = useRef(null);

    useEffect(() => {
        // Đăng ký DOM refs vào Context ngay khi component mount
        localVideoElRef.current  = localVideoDomRef.current;
        remoteVideoElRef.current = remoteVideoDomRef.current;

        console.log("[CallVideo] Registered video refs to Context");
        console.log("[CallVideo] localVideoEl:", !!localVideoDomRef.current);
        console.log("[CallVideo] remoteVideoEl:", !!remoteVideoDomRef.current);

        // Nếu đã có stream pending (race condition), gán lại ngay
        // Trường hợp: addremotestream fire trước component mount
        const ls = localStreamRef.current;
        if (ls && localVideoDomRef.current) {
            console.log("[CallVideo] Re-assigning pending local stream");
            localVideoDomRef.current.srcObject = null;
            localVideoDomRef.current.srcObject = ls;
            localVideoDomRef.current.play().catch(() => {});
        }

        return () => {
            // Khi unmount, clear refs trong Context
            localVideoElRef.current  = null;
            remoteVideoElRef.current = null;
        };
    }, []); // chỉ chạy 1 lần khi mount

    // Sync camera/mic button state với track state
    useEffect(() => {
        const stream = localStreamRef.current;
        if (stream) {
            const vt = stream.getVideoTracks();
            const at = stream.getAudioTracks();
            setIsCameraOn(vt.length > 0 && vt[0].enabled);
            setIsMicOn(at.length > 0 && at[0].enabled);
        }
    }, [currentCall]); // sync khi call bắt đầu

    const toggleCamera = () => {
        if (!mediaPermissions.video) return;
        const newState = !isCameraOn;
        toggleLocalVideo(!newState);
        setIsCameraOn(newState);
    };

    const toggleMicrophone = () => {
        if (!mediaPermissions.audio) return;
        const newState = !isMicOn;
        toggleMute(!newState);
        setIsMicOn(newState);
    };

    const handleClose = () => {
        cleanupCall("close button");
        if (onCallEnd) onCallEnd();
    };

    if (!currentCall && !isCallEnding) return null;

    return (
        <div className="fixed inset-0 bg-black z-[999] flex items-center justify-center">
            {isCallEnding ? (
                <div className="text-center text-white space-y-4">
                    <div className="text-6xl mb-4">📞</div>
                    <p className="text-2xl font-semibold">Cuộc gọi đã kết thúc</p>
                    <button
                        onClick={handleClose}
                        className="bg-blue-600 px-6 py-3 rounded-lg hover:bg-blue-700 font-medium"
                    >
                        Đóng
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

                    {/* ── Remote video — toàn màn hình ──
                        ✅ LUÔN render video tag (không conditional)
                        để remoteVideoDomRef.current không bao giờ null
                        Dùng CSS hidden thay vì unmount/mount             */}
                    <div className="absolute inset-0 z-[1] bg-gray-800">
                        {/* Placeholder khi chưa có stream */}
                        <div
                            id="remote-placeholder"
                            className="absolute inset-0 flex items-center justify-center"
                            style={{ display: "flex" }}
                        >
                            <div className="text-white text-center">
                                <div className="text-6xl mb-4">👤</div>
                                <p className="text-xl">Đang chờ video từ đối phương...</p>
                            </div>
                        </div>

                        {/* Remote video — luôn mount, ẩn bằng CSS khi chưa có stream */}
                        <video
                            ref={remoteVideoDomRef}
                            autoPlay
                            playsInline
                            className="absolute inset-0 w-full h-full object-cover"
                            onLoadedMetadata={() => {
                                // Ẩn placeholder khi video bắt đầu play
                                const ph = document.getElementById("remote-placeholder");
                                if (ph) ph.style.display = "none";
                            }}
                        />
                    </div>

                    {/* ── Local video — góc phải dưới ── */}
                    <div className="absolute bottom-28 right-4 w-32 h-48 md:w-44 md:h-60 bg-gray-900 rounded-xl overflow-hidden border-2 border-white z-10 shadow-xl">
                        <video
                            ref={localVideoDomRef}
                            autoPlay
                            playsInline
                            muted
                            className="w-full h-full object-cover"
                        />
                        {!isCameraOn && (
                            <div className="absolute inset-0 flex items-center justify-center bg-gray-900">
                                <span className="text-3xl">📷</span>
                            </div>
                        )}
                    </div>

                    {/* ── Controls ── */}
                    <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex items-center space-x-4 z-20">
                        <button
                            onClick={toggleCamera}
                            disabled={!mediaPermissions.video}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isCameraOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${!mediaPermissions.video ? "opacity-40 cursor-not-allowed" : ""}`}
                        >
                            <span className="text-lg">{isCameraOn ? "📹" : "📷"}</span>
                        </button>

                        <button
                            onClick={toggleMicrophone}
                            disabled={!mediaPermissions.audio}
                            className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-lg transition-colors
                                ${isMicOn ? "bg-gray-600 hover:bg-gray-700" : "bg-red-600 hover:bg-red-700"}
                                ${!mediaPermissions.audio ? "opacity-40 cursor-not-allowed" : ""}`}
                        >
                            <span className="text-lg">{isMicOn ? "🎤" : "🔇"}</span>
                        </button>

                        <button
                            onClick={endCall}
                            className="flex items-center space-x-2 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-full shadow-lg transition-colors font-medium"
                        >
                            <span>📞</span>
                            <span>Kết thúc</span>
                        </button>
                    </div>

                    {/* Indicators */}
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
                    </div>
                </>
            )}
        </div>
    );
};

export default CallVideo;