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
    const [isVideoCall, setIsVideoCall] = useState(false);
    const [mediaPermissions, setMediaPermissions] = useState({
        audio: false,
        video: false,
    });

    const clientRef        = useRef(null);
    const currentCallRef   = useRef(null);
    const localStreamRef   = useRef(null);
    const remoteStreamRef  = useRef(null);
    const eventsSetupRef   = useRef(false);

    const remoteVideoElRef = useRef(null);
    const localVideoElRef  = useRef(null);

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
            script.onload = () => console.log("[Stringee] SDK loaded ✅");
            document.body.appendChild(script);
        }
    }, []);

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
        videoRef.current.srcObject = null;
        videoRef.current.srcObject = stream;
        videoRef.current.play().catch((e) => {
            console.warn("[Stringee] autoplay failed:", e.message);
        });
    }, []);

    const createMediaStream = useCallback(async (isVid = false) => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                audio: true,
                video: isVid
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

        [localStreamRef.current, remoteStreamRef.current].forEach((s) => {
            if (s) s.getTracks().forEach((t) => { t.stop(); t.enabled = false; });
        });
        localStreamRef.current  = null;
        remoteStreamRef.current = null;

        if (localVideoElRef.current)  localVideoElRef.current.srcObject  = null;
        if (remoteVideoElRef.current) remoteVideoElRef.current.srcObject = null;

        eventsSetupRef.current  = false;
        currentCallRef.current  = null;

        setCurrentCall(null);
        setIncomingCaller(null);
        setIsCallEnding(false);
        setCallStatus("Cleaned");
        setIsVideoCall(false);
        setMediaPermissions({ audio: false, video: false });
    }, []);

    const setupCallEvents = useCallback(
        (call) => {
            if (eventsSetupRef.current) {
                console.log("[Stringee] events already set up, skip");
                return;
            }
            eventsSetupRef.current = true;
            console.log("[Stringee] setupCallEvents");

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
            console.log("[Stringee] incomingcall 📞 isVideoCall:", call.isVideoCall);
            playRingtone("/ringtone.mp3", { loop: true, duration: 30000, volume: 0.8 });
            eventsSetupRef.current = false;
            currentCallRef.current = call;
            setIsVideoCall(!!call.isVideoCall);
            onIncomingCall(call);
        });

        client.on("requestnewtoken", () => {
            console.warn("[Stringee] token expired");
            onConnChange(false);
        });

        return client;
    }

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
                        isVideoCall: !!incomingCall.isVideoCall,
                    });
                },
                (connected) => {
                    setIsConnected(connected);
                    setCallStatus(connected ? "Connected" : "Disconnected");
                }
            );
        }
    }, [token]);

    const makeCall = useCallback(
        async (callee, isVid = false) => {
            eventsSetupRef.current = false;
            console.log("[Stringee] makeCall →", callee, "video:", isVid);
            setIsVideoCall(isVid);

            const stream = await createMediaStream(isVid);
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
                    isVid
                );

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

    const acceptCall = useCallback(async () => {
        const call = currentCallRef.current;
        if (!call) return;
        const isVid = !!call.isVideoCall;
        console.log("[Stringee] acceptCall, isVideoCall:", isVid);
        stopSound();
        setIsVideoCall(isVid);

        const stream = await createMediaStream(isVid);
        if (!stream) return;

        call.localStream = stream;
        localStreamRef.current = stream;
        assignStreamToVideo(localVideoElRef, stream);

        setupCallEvents(call);

        setIncomingCaller(null);
        setCurrentCall(call);

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                console.log("[Stringee] calling answer()");
                call.answer();
            });
        });
    }, [createMediaStream, setupCallEvents, assignStreamToVideo]);

    const rejectCall = useCallback(() => {
        stopSound();
        const call = currentCallRef.current;
        if (!call) return;
        call.reject(() => cleanupCall("rejected"));
    }, [cleanupCall]);

    const endCall = useCallback(() => {
        const call = currentCall || currentCallRef.current;
        if (!call) return;
        setIsCallEnding(true);
        call.hangup(() => setTimeout(() => cleanupCall("hangup"), 1500));
    }, [currentCall, cleanupCall]);

    const toggleMute = useCallback((muted) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        stream.getAudioTracks().forEach((t) => (t.enabled = !muted));
        const call = currentCallRef.current;
        if (call?.mute) call.mute(muted);
    }, []);

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
                isConnected,
                currentCall,
                callStatus,
                incomingCaller,
                isCallEnding,
                callerName,
                isVideoCall,
                mediaPermissions,
                remoteVideoElRef,
                localVideoElRef,
                localStreamRef,
                remoteStreamRef,
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