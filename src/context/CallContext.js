"use client";

import React, {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";
import { jwtDecode } from "jwt-decode";
import {
    initAudioSystem,
    playRingtone,
    preloadAudio,
    stopSound,
} from "@/utils/playSound";
import api from "@/utils/axios";
import toast from "react-hot-toast";

function decodeJWT(token) {
    try {
        return jwtDecode(token);
    } catch (e) {
        return null;
    }
}

const CallContext = createContext();

export const CallProvider = ({ children }) => {
    const [token, setToken] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [incomingCaller, setIncomingCaller] = useState(null);
    const [currentCall, setCurrentCall] = useState(null);
    const [callStatus, setCallStatus] = useState("Chưa kết nối");
    const [isCallEnding, setIsCallEnding] = useState(false);
    const [callerName, setCallerName] = useState("");
    const [mediaPermissions, setMediaPermissions] = useState({
        audio: false,
        video: false,
    });

    // ── Refs ──────────────────────────────────────────────────────────
    const clientRef        = useRef(null);
    const currentCallRef   = useRef(null);
    const localStreamRef   = useRef(null);
    const remoteStreamRef  = useRef(null);
    const eventsSetupRef   = useRef(false);

    // ✅ KEY FIX: video element refs được giữ ở Context
    // CallVideo đăng ký refs này vào Context để khi stream arrive
    // có thể gán thẳng vào DOM — KHÔNG qua React state (tránh race condition)
    const remoteVideoElRef = useRef(null);
    const localVideoElRef  = useRef(null);

    // ── Audio init ────────────────────────────────────────────────────
    useEffect(() => {
        initAudioSystem();
        preloadAudio("/ringtone.mp3");
        if ("Notification" in window && Notification.permission === "default") {
            Notification.requestPermission();
        }
    }, []);

    // ── Load Stringee SDK ─────────────────────────────────────────────
    useEffect(() => {
        if (typeof window !== "undefined" && !window.StringeeClient) {
            const script = document.createElement("script");
            script.src = "/libs/latest.sdk.bundle.min.js";
            script.async = true;
            script.onload = () => console.log("[Stringee] SDK loaded ✅");
            document.body.appendChild(script);
        }
    }, []);

    // ── Helpers ───────────────────────────────────────────────────────

    /** Gán stream vào <video> element ngay lập tức, không qua React state */
    const assignStreamToVideo = useCallback((videoRef, stream) => {
        if (!videoRef.current) {
            console.warn("[Stringee] video element ref is null, cannot assign stream");
            return;
        }
        if (!stream) {
            console.warn("[Stringee] stream is null");
            return;
        }
        console.log("[Stringee] ✅ Assigning stream to video, tracks:", stream.getTracks().length);
        // Reset srcObject trước (workaround Chrome/Edge bug theo Stringee docs)
        videoRef.current.srcObject = null;
        videoRef.current.srcObject = stream;
        videoRef.current.play().catch((e) => {
            console.warn("[Stringee] autoplay failed:", e.message);
        });
    }, []);

    const createMediaStream = useCallback(async (isVideo = false) => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                audio: true,
                video: isVideo
                    ? { width: { ideal: 1280 }, height: { ideal: 720 }, facingMode: "user" }
                    : false,
            });
            setMediaPermissions({
                audio: stream.getAudioTracks().length > 0,
                video: stream.getVideoTracks().length > 0,
            });
            return stream;
        } catch (err) {
            console.error("[Stringee] getUserMedia error:", err);
            setMediaPermissions({ audio: false, video: false });
            return null;
        }
    }, []);

    const cleanupCall = useCallback((reason) => {
        console.log("[Stringee] cleanupCall:", reason);
        stopSound();

        // Stop tất cả tracks
        [localStreamRef.current, remoteStreamRef.current].forEach((s) => {
            if (s) s.getTracks().forEach((t) => { t.stop(); t.enabled = false; });
        });
        localStreamRef.current  = null;
        remoteStreamRef.current = null;

        // Clear video elements
        if (localVideoElRef.current)  localVideoElRef.current.srcObject  = null;
        if (remoteVideoElRef.current) remoteVideoElRef.current.srcObject = null;

        eventsSetupRef.current  = false;
        currentCallRef.current  = null;

        setCurrentCall(null);
        setIncomingCaller(null);
        setIsCallEnding(false);
        setCallStatus("Cleaned");
        setMediaPermissions({ audio: false, video: false });
    }, []);

    // ── setupCallEvents ───────────────────────────────────────────────
    const setupCallEvents = useCallback(
        (call) => {
            if (eventsSetupRef.current) {
                console.log("[Stringee] events already set up, skip");
                return;
            }
            eventsSetupRef.current = true;
            console.log("[Stringee] setupCallEvents");

            // ✅ Gán thẳng vào DOM ref — KHÔNG setState
            // Đây là pattern chính xác theo Stringee docs
            call.on("addremotestream", (stream) => {
                console.log("[Stringee] ✅ addremotestream, tracks:", stream?.getTracks?.()?.length ?? "?");
                remoteStreamRef.current = stream;
                assignStreamToVideo(remoteVideoElRef, stream);
                setCallStatus("Connected - Remote stream received");
            });

            call.on("addlocalstream", (stream) => {
                console.log("[Stringee] ✅ addlocalstream, tracks:", stream?.getTracks?.()?.length ?? "?");
                localStreamRef.current = stream;
                assignStreamToVideo(localVideoElRef, stream);
            });

            call.on("signalingstate", (state) => {
                console.log("[Stringee] signalingstate:", state.reason, state.code);
                if (state.reason === "answered") {
                    setCallStatus("Đang kết nối...");
                    stopSound();
                } else if (["Ended", "Busy here", "Rejected", "Disconnected"].includes(state.reason)) {
                    setIsCallEnding(true);
                    setTimeout(() => cleanupCall(state.reason), 1500);
                }
            });

            call.on("mediastate", (state) => {
                console.log("[Stringee] mediastate:", state);
                if (state.code === 1) {
                    setCallStatus("Connected");
                } else if (state.code === 0) {
                    setIsCallEnding(true);
                    setTimeout(() => cleanupCall("media disconnected"), 1500);
                }
            });

            call.on("disconnect", () => {
                console.log("[Stringee] disconnect");
                setIsCallEnding(true);
                setTimeout(() => cleanupCall("disconnect event"), 1500);
            });
        },
        [cleanupCall, assignStreamToVideo]
    );

    // ── Stringee client ───────────────────────────────────────────────
    function connectStringeeClient(accessToken, onIncomingCall, onConnChange) {
        const client = new window.StringeeClient();
        client.connect(accessToken);

        client.on("connect", () => {
            console.log("[Stringee] client connected ✅");
            onConnChange(true);
        });

        client.on("disconnect", () => {
            console.warn("[Stringee] client disconnected");
            onConnChange(false);
        });

        client.on("incomingcall", (call) => {
            console.log("[Stringee] incomingcall 📞");
            playRingtone("/ringtone.mp3", { loop: true, duration: 30000, volume: 0.8 });
            eventsSetupRef.current = false;
            currentCallRef.current = call;
            onIncomingCall(call);
        });

        client.on("requestnewtoken", () => {
            console.warn("[Stringee] token expired");
            onConnChange(false);
        });

        return client;
    }

    // ── initializeCall ────────────────────────────────────────────────
    const initializeCall = useCallback(async (beToken) => {
        const payload = decodeJWT(beToken);
        if (payload?.username) setCallerName(payload.username);
        try {
            const res = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL}/v1/stringee/create-token`,
                { method: "POST", headers: { Authorization: `Bearer ${beToken}` } }
            );
            const data = await res.json();
            if (data.body?.token) setToken(data.body.token);
        } catch {
            setCallStatus("Token fetch failed");
        }
    }, []);

    useEffect(() => {
        if (token && window.StringeeClient) {
            clientRef.current = connectStringeeClient(
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
        }
    }, [token]);

    // ── makeCall ──────────────────────────────────────────────────────
    const makeCall = useCallback(
        async (callee, isVideo = false) => {
            eventsSetupRef.current = false;
            console.log("[Stringee] makeCall →", callee, "video:", isVideo);

            const stream = await createMediaStream(isVideo);
            if (!stream) return;

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
                    isVideo
                );

                // Gán localStream vào call object và video element
                call.localStream = stream;
                localStreamRef.current = stream;
                assignStreamToVideo(localVideoElRef, stream);

                setupCallEvents(call);

                currentCallRef.current = call;
                setCurrentCall(call);
                setCallStatus("Đang kết nối...");

                call.makeCall((res) => {
                    if (res.r === 0) {
                        setCallStatus("Đang đổ chuông...");
                    } else {
                        console.error("[Stringee] makeCall failed:", res.r, res.message);
                        stream.getTracks().forEach((t) => t.stop());
                        cleanupCall("makeCall failed");
                    }
                });
            } catch (err) {
                if (err?.response?.data?.code === 7012 || err?.response?.data?.code === 7011)
                    toast.error("Máy bận");
                stream.getTracks().forEach((t) => t.stop());
                cleanupCall("init failed");
            }
        },
        [callerName, createMediaStream, setupCallEvents, cleanupCall, assignStreamToVideo]
    );

    // ── acceptCall ────────────────────────────────────────────────────
    const acceptCall = useCallback(async () => {
        const call = currentCallRef.current;
        if (!call) return;
        console.log("[Stringee] acceptCall, isVideo:", call.isVideoCall);
        stopSound();

        const stream = await createMediaStream(call.isVideoCall);
        if (!stream) return;

        // Gán stream vào call object và video element ngay lập tức
        call.localStream = stream;
        localStreamRef.current = stream;
        assignStreamToVideo(localVideoElRef, stream);

        // Setup events TRƯỚC answer để không bỏ lỡ addremotestream
        setupCallEvents(call);

        // Set state để render CallVideo
        setIncomingCaller(null);
        setCurrentCall(call);

        // Gọi answer() — addremotestream có thể fire ngay trong lệnh này
        // Vì remoteVideoElRef đã được đăng ký từ CallVideo component
        // (CallVideo mount ngay khi setCurrentCall xong vì layout.js render sync)
        // Dùng requestAnimationFrame để chắc chắn DOM đã paint
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                console.log("[Stringee] calling answer()");
                call.answer();
            });
        });
    }, [createMediaStream, setupCallEvents, assignStreamToVideo]);

    // ── rejectCall ────────────────────────────────────────────────────
    const rejectCall = useCallback(() => {
        stopSound();
        const call = currentCallRef.current;
        if (!call) return;
        call.reject(() => cleanupCall("rejected"));
    }, [cleanupCall]);

    // ── endCall ───────────────────────────────────────────────────────
    const endCall = useCallback(() => {
        const call = currentCall || currentCallRef.current;
        if (!call) return;
        setIsCallEnding(true);
        call.hangup(() => setTimeout(() => cleanupCall("hangup"), 1500));
    }, [currentCall, cleanupCall]);

    // ── toggleMute ────────────────────────────────────────────────────
    const toggleMute = useCallback((muted) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        stream.getAudioTracks().forEach((t) => (t.enabled = !muted));
        const call = currentCallRef.current;
        if (call?.mute) call.mute(muted);
    }, []);

    // ── toggleLocalVideo ──────────────────────────────────────────────
    const toggleLocalVideo = useCallback((enabled) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        stream.getVideoTracks().forEach((t) => (t.enabled = !enabled));
        const call = currentCallRef.current;
        if (call?.enableLocalVideo) call.enableLocalVideo(!enabled);
    }, []);

    return (
        <CallContext.Provider
            value={{
                // state
                isConnected,
                currentCall,
                callStatus,
                incomingCaller,
                isCallEnding,
                callerName,
                mediaPermissions,
                // refs expose ra để CallVideo đăng ký
                remoteVideoElRef,
                localVideoElRef,
                // streams (để check track state cho UI buttons)
                localStreamRef,
                remoteStreamRef,
                // actions
                initializeCall,
                makeCall,
                acceptCall,
                rejectCall,
                endCall,
                toggleMute,
                toggleLocalVideo,
                cleanupCall,
            }}
        >
            {children}
        </CallContext.Provider>
    );
};

export const useCall = () => {
    const ctx = useContext(CallContext);
    if (!ctx) throw new Error("useCall must be used within CallProvider");
    return ctx;
};