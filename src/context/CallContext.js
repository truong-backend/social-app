"use client";

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { initAudioSystem, playRingtone, preloadAudio, stopSound } from "@/utils/playSound";
import api from "@/utils/axios";
import toast from "react-hot-toast";

function decodeJWT(token) {
    try {
        return jwtDecode(token);
    } catch (e) {
        console.error("[DEBUG] Failed to decode JWT:", e);
        return null;
    }
}

const CallContext = createContext();

export const CallProvider = ({ children }) => {
    const [token, setToken] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [incomingCaller, setIncomingCaller] = useState(null);
    const [currentCall, setCurrentCall] = useState(null);
    const [callStatus, setCallStatus] = useState("chưa có gì xảy ra");
    const [remoteStream, setRemoteStream] = useState(null);
    const [localStream, setLocalStream] = useState(null);
    const [callerName, setCallerName] = useState("");
    const [isCallEnding, setIsCallEnding] = useState(false);
    const [mediaPermissions, setMediaPermissions] = useState({ audio: false, video: false });

    const clientRef = useRef(null);
    const currentCallRef = useRef(null);
    const beTokenRef = useRef("");
    const localStreamRef = useRef(null);
    const remoteStreamRef = useRef(null);
    // ✅ Flag: setupCallEvents chỉ chạy 1 lần mỗi cuộc gọi
    const eventsSetupRef = useRef(false);

    useEffect(() => {
        initAudioSystem();
        preloadAudio("/ringtone.mp3");
        if ("Notification" in window && Notification.permission === "default") {
            Notification.requestPermission();
        }
    }, []);

    useEffect(() => {
        if (typeof window !== "undefined" && !window.StringeeClient) {
            const script = document.createElement("script");
            script.src = "/libs/latest.sdk.bundle.min.js";
            script.async = true;
            script.onload = () => console.log("[DEBUG] Stringee SDK loaded ✅");
            document.body.appendChild(script);
        }
    }, []);

    const createMediaStream = useCallback(async (isVideo = false) => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                audio: true,
                video: isVideo ? { width: { ideal: 1280 }, height: { ideal: 720 }, facingMode: "user" } : false,
            });
            setMediaPermissions({
                audio: stream.getAudioTracks().length > 0,
                video: stream.getVideoTracks().length > 0,
            });
            return stream;
        } catch (error) {
            console.error("[DEBUG] Media error:", error);
            setCallStatus("Permission denied: " + error.message);
            setMediaPermissions({ audio: false, video: false });
            return null;
        }
    }, []);

    const cleanupCall = useCallback((stt) => {
        console.log("[DEBUG] cleanupCall:", stt);
        stopSound();

        const ls = localStreamRef.current;
        const rs = remoteStreamRef.current;
        if (ls) ls.getTracks().forEach((t) => { t.stop(); t.enabled = false; });
        if (rs) rs.getTracks().forEach((t) => { t.stop(); t.enabled = false; });

        localStreamRef.current = null;
        remoteStreamRef.current = null;
        eventsSetupRef.current = false;
        currentCallRef.current = null;

        setRemoteStream(null);
        setLocalStream(null);
        setCurrentCall(null);
        setIncomingCaller(null);
        setCallStatus("Cleaned");
        setMediaPermissions({ audio: false, video: false });
        setIsCallEnding(false);
    }, []);

    const setupCallEvents = useCallback(
        (call) => {
            // ✅ Guard: chỉ setup 1 lần
            if (eventsSetupRef.current) {
                console.log("[DEBUG] Events already set up, skip");
                return;
            }
            eventsSetupRef.current = true;
            console.log("[DEBUG] setupCallEvents — binding events");

            call.on("addremotestream", (stream) => {
                const realStream = stream?.stream || stream;
                if (realStream) {
                    console.log("[DEBUG] ✅ addremotestream — tracks:", realStream.getTracks().length);
                    remoteStreamRef.current = realStream;
                    setRemoteStream(realStream);
                    setCallStatus("Connected - Remote stream received");
                } else {
                    console.error("[DEBUG] ❌ addremotestream — stream null");
                }
            });

            call.on("addlocalstream", (stream) => {
                const realStream = stream?.stream || stream;
                if (realStream) {
                    console.log("[DEBUG] ✅ addlocalstream — tracks:", realStream.getTracks().length);
                    localStreamRef.current = realStream;
                    setLocalStream(realStream);
                } else {
                    console.error("[DEBUG] ❌ addlocalstream — stream null");
                }
            });

            call.on("signalingstate", (state) => {
                console.log("[DEBUG] signalingstate:", state);
                if (state.reason === "answered") {
                    setCallStatus("Call answered");
                    stopSound();
                } else if (["Ended", "Busy here", "Rejected", "Disconnected"].includes(state.reason)) {
                    setIsCallEnding(true);
                    setTimeout(() => cleanupCall(1), 1500);
                }
            });

            call.on("mediastate", (state) => {
                console.log("[DEBUG] mediastate:", state);
                if (state.code === 0) {
                    setIsCallEnding(true);
                    setTimeout(() => cleanupCall(2), 1500);
                }
            });

            call.on("disconnect", () => {
                console.log("[DEBUG] call disconnect");
                setIsCallEnding(true);
                setTimeout(() => cleanupCall(3), 1500);
            });

            call.on("remotevideostatuschange", (e) => console.log("[DEBUG] remotevideostatuschange:", e));
            call.on("remoteaudiostatuschange", (e) => console.log("[DEBUG] remoteaudiostatuschange:", e));
        },
        [cleanupCall]
    );

    function connectStringeeClient(token, onIncomingCall, onConnectionChange) {
        const client = new window.StringeeClient();
        client.connect(token);

        client.on("connect", () => {
            console.log("[DEBUG] Stringee connected ✅");
            onConnectionChange(true);
        });

        client.on("disconnect", () => {
            console.warn("[DEBUG] Stringee disconnected ❌");
            onConnectionChange(false);
        });

        client.on("incomingcall", (call) => {
            console.log("[DEBUG] incomingcall 📞");
            playRingtone("/ringtone.mp3", { loop: true, duration: 30000, volume: 0.8 });

            // ✅ CHỈ lưu ref, KHÔNG setupCallEvents ở đây
            // setupCallEvents gọi DUY NHẤT trong acceptCall
            eventsSetupRef.current = false;
            currentCallRef.current = call;
            onIncomingCall(call);
        });

        client.on("requestnewtoken", () => {
            console.warn("[DEBUG] Token expired");
            onConnectionChange(false);
        });

        return client;
    }

    const initializeCall = useCallback(async (beToken) => {
        beTokenRef.current = beToken;
        const payload = decodeJWT(beToken);
        if (payload?.username) setCallerName(payload.username);
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/v1/stringee/create-token`, {
                method: "POST",
                headers: { Authorization: `Bearer ${beToken}` },
            });
            const data = await res.json();
            if (data.body?.token) setToken(data.body.token);
        } catch (err) {
            setCallStatus("Token fetch failed");
        }
    }, []);

    useEffect(() => {
        if (token && window.StringeeClient) {
            const client = connectStringeeClient(
                token,
                (incomingCall) => {
                    setIncomingCaller({
                        name: incomingCall.fromAlias || incomingCall.fromNumber,
                        profilePictureUrl: incomingCall.customDataFromYourServer,
                    });
                },
                (connected) => {
                    setIsConnected(connected);
                    setCallStatus(connected ? "Connected" : "Disconnected");
                }
            );
            clientRef.current = client;
        }
    }, [token]);

    const makeCall = useCallback(
        async (callee, isVideo = false) => {
            console.log("[DEBUG] makeCall →", callee, "video:", isVideo);
            eventsSetupRef.current = false;

            const stream = await createMediaStream(isVideo);
            if (!stream) {
                setCallStatus("Media permission denied");
                return;
            }

            try {
                const res = await api.get(`/v1/call/init/${callee.trim()}`);
                if (res.data.code === 7012 || res.data.code === 7011) {
                    toast.error("Máy bận");
                    stream.getTracks().forEach((t) => t.stop());
                    return;
                }

                const call = new window.StringeeCall(
                    clientRef.current,
                    callerName,
                    callee.trim(),
                    isVideo,
                    { audio: true, video: isVideo }
                );

                // ✅ Gán stream TRƯỚC setupCallEvents
                call.localStream = stream;
                localStreamRef.current = stream;
                setLocalStream(stream);

                setupCallEvents(call);

                currentCallRef.current = call;
                setCurrentCall(call);
                setCallStatus("Initiating call...");

                call.makeCall((res) => {
                    if (res.r === 0) {
                        setCallStatus("Ringing...");
                    } else {
                        console.error("[DEBUG] makeCall failed:", res.r, res.message);
                        setCallStatus(`Call failed: ${res.message || "Unknown error"}`);
                        stream.getTracks().forEach((t) => t.stop());
                        cleanupCall(4);
                    }
                });
            } catch (error) {
                if (error?.response?.data?.code === 7012 || error?.response?.data?.code === 7011)
                    toast.error("Máy bận");
                stream.getTracks().forEach((t) => t.stop());
                setCallStatus("Init call failed");
                cleanupCall(4);
            }
        },
        [callerName, createMediaStream, setupCallEvents, cleanupCall]
    );

    const acceptCall = useCallback(async () => {
        const call = currentCallRef.current;
        if (!call) return;

        console.log("[DEBUG] acceptCall — isVideo:", call.isVideoCall);
        stopSound();

        const stream = await createMediaStream(call.isVideoCall);
        if (!stream) return;

        // ✅ Gán stream TRƯỚC setupCallEvents
        call.localStream = stream;
        localStreamRef.current = stream;
        setLocalStream(stream);

        // ✅ setupCallEvents chỉ chạy 1 lần nhờ eventsSetupRef
        setupCallEvents(call);

        // ✅ setCurrentCall → React render CallVideo (và <video ref={remoteVideoRef}> mount)
        setIncomingCaller(null);
        setCurrentCall(call);

        // ✅ Tăng delay lên 50ms để React chắc chắn đã commit DOM
        // (0ms đôi khi không đủ trên thiết bị chậm)
        setTimeout(() => {
            console.log("[DEBUG] call.answer()");
            call.answer();
        }, 50);
    }, [createMediaStream, setupCallEvents]);

    const rejectCall = useCallback(() => {
        const call = currentCallRef.current;
        stopSound();
        if (!call) return;
        call.reject(() => cleanupCall(5));
    }, [cleanupCall]);

    const endCall = useCallback(() => {
        const call = currentCall || currentCallRef.current;
        if (!call) return;
        setIsCallEnding(true);
        setCallStatus("Ending call...");
        call.hangup(() => setTimeout(() => cleanupCall(6), 1500));
    }, [currentCall, cleanupCall]);

    const toggleMute = useCallback((muted) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        const tracks = stream.getAudioTracks();
        if (tracks.length === 0) return;
        tracks.forEach((t) => (t.enabled = !muted));
        const call = currentCallRef.current;
        if (call && typeof call.mute === "function") call.mute(muted);
    }, []);

    const toggleLocalVideo = useCallback((enabled) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        const tracks = stream.getVideoTracks();
        if (tracks.length === 0) return;
        tracks.forEach((t) => (t.enabled = !enabled));
        const call = currentCallRef.current;
        if (call && typeof call.enableLocalVideo === "function") call.enableLocalVideo(!enabled);
    }, []);

    return (
        <CallContext.Provider
            value={{
                isConnected,
                currentCall,
                callStatus,
                incomingCaller,
                remoteStream,
                localStream,
                callerName,
                isCallEnding,
                mediaPermissions,
                initializeCall,
                makeCall,
                acceptCall,
                rejectCall,
                endCall,
                toggleMute,
                toggleLocalVideo,
                cleanupCall,
                createMediaStream,
            }}
        >
            {children}
        </CallContext.Provider>
    );
};

export const useCall = () => {
    const context = useContext(CallContext);
    if (context === undefined) throw new Error("useCall must be used within a CallProvider ❌");
    return context;
};