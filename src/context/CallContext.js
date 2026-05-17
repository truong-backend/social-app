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

// Lấy STOMP client từ socket.js (đã có sẵn trong project)
// Giả sử socket.js export một hàm getStompClient() hoặc stompClient instance
// Nếu không, sẽ tạo kết nối riêng bên dưới

function decodeJWT(token) {
    try {
        return jwtDecode(token);
    } catch (e) {
        return null;
    }
}

const CallContext = createContext();

// ICE servers — dùng STUN public của Google
const ICE_SERVERS = {
    iceServers: [
        { urls: "stun:stun.l.google.com:19302" },
        { urls: "stun:stun1.l.google.com:19302" },
    ],
};

export const CallProvider = ({ children }) => {
    const [isConnected, setIsConnected]       = useState(false);
    const [incomingCaller, setIncomingCaller] = useState(null);
    const [currentCall, setCurrentCall]       = useState(null); // { callId, callee, isVideoCall }
    const [callStatus, setCallStatus]         = useState("Chưa kết nối");
    const [isCallEnding, setIsCallEnding]     = useState(false);
    const [isVideoCall, setIsVideoCall]       = useState(false);
    const [mediaPermissions, setMediaPermissions] = useState({ audio: false, video: false });

    const myUserIdRef      = useRef(null); // userId (UUID) từ JWT — dùng làm địa chỉ signal
    const myUsernameRef    = useRef(null);
    const stompClientRef   = useRef(null);
    const signalSubRef     = useRef(null);
    const pcRef            = useRef(null); // RTCPeerConnection
    const localStreamRef   = useRef(null);
    const remoteStreamRef  = useRef(null);
    const pendingCandidatesRef = useRef([]);
    const remoteDescSetRef = useRef(false);
    const currentCallIdRef = useRef(null);
    const calleeIdRef      = useRef(null); // userId của người kia (để gửi signal)
    const isOfferPendingRef = useRef(false);

    const remoteVideoElRef = useRef(null);
    const localVideoElRef  = useRef(null);

    useEffect(() => {
        initAudioSystem();
        preloadAudio("/ringtone.mp3");
        if ("Notification" in window && Notification.permission === "default") {
            Notification.requestPermission();
        }
    }, []);

    // ── Assign stream to video element ──────────────────────────────
    const assignStreamToVideo = useCallback((videoRef, stream) => {
        if (!videoRef.current || !stream) return;
        videoRef.current.srcObject = null;
        videoRef.current.srcObject = stream;
        videoRef.current.play().catch(() => {});
    }, []);

    // ── Get user media ───────────────────────────────────────────────
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
            console.error("[WebRTC] getUserMedia error:", err);
            setMediaPermissions({ audio: false, video: false });
            return null;
        }
    }, []);

    // ── Send signal via STOMP ────────────────────────────────────────
    const sendSignal = useCallback((toUserId, type, payload) => {
        const stomp = stompClientRef.current;
        if (!stomp || !stomp.connected) {
            console.warn("[WebRTC] STOMP not connected, cannot send signal");
            return;
        }
        stomp.publish({
            destination: "/app/signal",
            body: JSON.stringify({ to: toUserId, type, payload }),
        });
    }, []);

    // ── Cleanup call ─────────────────────────────────────────────────
    const cleanupCall = useCallback((reason) => {
        console.log("[WebRTC] cleanupCall:", reason);
        stopSound();

        if (pcRef.current) {
            pcRef.current.ontrack = null;
            pcRef.current.onicecandidate = null;
            pcRef.current.close();
            pcRef.current = null;
        }

        [localStreamRef.current, remoteStreamRef.current].forEach((s) => {
            if (s) s.getTracks().forEach((t) => { t.stop(); t.enabled = false; });
        });
        localStreamRef.current  = null;
        remoteStreamRef.current = null;

        if (localVideoElRef.current)  localVideoElRef.current.srcObject  = null;
        if (remoteVideoElRef.current) remoteVideoElRef.current.srcObject = null;

        pendingCandidatesRef.current = [];
        remoteDescSetRef.current     = false;
        currentCallIdRef.current     = null;
        calleeIdRef.current          = null;
        isOfferPendingRef.current    = false;

        setCurrentCall(null);
        setIncomingCaller(null);
        setIsCallEnding(false);
        setCallStatus("Cleaned");
        setIsVideoCall(false);
        setMediaPermissions({ audio: false, video: false });
    }, []);

    // ── Create RTCPeerConnection ─────────────────────────────────────
    const createPeerConnection = useCallback((remoteUserId, isVid) => {
        const pc = new RTCPeerConnection(ICE_SERVERS);
        pcRef.current = pc;

        pc.onicecandidate = (event) => {
            if (event.candidate) {
                sendSignal(remoteUserId, "candidate", JSON.stringify(event.candidate));
            }
        };

        pc.ontrack = (event) => {
            console.log("[WebRTC] ✅ ontrack", event.streams[0]?.getTracks()?.length);
            remoteStreamRef.current = event.streams[0];
            assignStreamToVideo(remoteVideoElRef, event.streams[0]);
            setCallStatus("Connected");
        };

        pc.onconnectionstatechange = () => {
            console.log("[WebRTC] connectionState:", pc.connectionState);
            if (["disconnected", "failed", "closed"].includes(pc.connectionState)) {
                setIsCallEnding(true);
                setTimeout(() => cleanupCall("connection state: " + pc.connectionState), 1500);
            }
        };

        return pc;
    }, [sendSignal, assignStreamToVideo, cleanupCall]);

    // ── Handle incoming WebRTC signals ───────────────────────────────
    const handleSignal = useCallback(async (signal) => {
        const { from, type, payload } = signal;
        console.log("[WebRTC] handleSignal:", type, "from:", from);

        if (type === "offer") {
            // Incoming call — lưu calleeId là người offer
            calleeIdRef.current = from;
            const offerData = JSON.parse(payload);
            const isVid = offerData.isVideoCall ?? false;
            setIsVideoCall(isVid);
            isOfferPendingRef.current = true;

            // Show incoming call popup
            playRingtone("/ringtone.mp3", { loop: true, duration: 30000, volume: 0.8 });
            setIncomingCaller({
                name: offerData.callerName || from,
                profilePictureUrl: offerData.callerAvatar || null,
                isVideoCall: isVid,
                sdp: offerData.sdp,
                fromUserId: from,
            });
            return;
        }

        if (type === "answer") {
            const pc = pcRef.current;
            if (!pc) return;
            const answerData = JSON.parse(payload);
            await pc.setRemoteDescription(new RTCSessionDescription(answerData.sdp));
            remoteDescSetRef.current = true;
            // Flush pending ICE candidates
            for (const c of pendingCandidatesRef.current) {
                await pc.addIceCandidate(new RTCIceCandidate(c));
            }
            pendingCandidatesRef.current = [];
            setCallStatus("Connected");
            stopSound();
            return;
        }

        if (type === "candidate") {
            const candidate = JSON.parse(payload);
            const pc = pcRef.current;
            if (pc && remoteDescSetRef.current) {
                await pc.addIceCandidate(new RTCIceCandidate(candidate));
            } else {
                pendingCandidatesRef.current.push(candidate);
            }
            return;
        }

        if (type === "reject" || type === "end") {
            setIsCallEnding(true);
            setTimeout(() => cleanupCall(type), 1500);
            return;
        }
    }, [cleanupCall]);

    // ── Subscribe to signal channel ──────────────────────────────────
    const subscribeToSignals = useCallback((stomp, userId) => {
        if (signalSubRef.current) {
            signalSubRef.current.unsubscribe();
        }
        signalSubRef.current = stomp.subscribe(
            `/signal/${userId}`,
            (msg) => {
                try {
                    handleSignal(JSON.parse(msg.body));
                } catch (e) {
                    console.error("[WebRTC] signal parse error", e);
                }
            }
        );
        console.log("[WebRTC] Subscribed to /signal/" + userId);
    }, [handleSignal]);

    // ── Initialize (gọi từ layout khi có token) ──────────────────────
    const initializeCall = useCallback(async (beToken) => {
        const payload = decodeJWT(beToken);
        if (!payload) return;

        const userId = payload.sub || payload.userId || payload.id;
        const username = payload.username || payload.sub;
        myUserIdRef.current   = userId;
        myUsernameRef.current = username;

        // Dùng lại STOMP client từ socket.js nếu có, hoặc chờ nó connect
        // Thử lấy từ window nếu socket.js expose ra global
        const checkStomp = setInterval(() => {
            const stomp = window.__stompClient;
            if (stomp && stomp.connected) {
                clearInterval(checkStomp);
                stompClientRef.current = stomp;
                setIsConnected(true);
                setCallStatus("Connected");
                subscribeToSignals(stomp, userId);
            }
        }, 500);

        // Timeout sau 30s
        setTimeout(() => clearInterval(checkStomp), 30000);
    }, [subscribeToSignals]);

    // ── Make call ────────────────────────────────────────────────────
    const makeCall = useCallback(async (calleeUsername, isVid = false, calleeUserId = null) => {
        console.log("[WebRTC] makeCall →", calleeUsername, "video:", isVid);
        setIsVideoCall(isVid);

        // Init check (busy check)
        try {
            const res = await api.get(`/v1/call/init/${calleeUsername.trim()}`);
            if (res.data.code === 7012 || res.data.code === 7011) {
                toast.error("Máy bận");
                return;
            }
        } catch (err) {
            if (err?.response?.data?.code === 7012 || err?.response?.data?.code === 7011) {
                toast.error("Máy bận");
            }
            return;
        }

        const stream = await createMediaStream(isVid);
        if (!stream) return;
        localStreamRef.current = stream;
        assignStreamToVideo(localVideoElRef, stream);

        // calleeId là userId của callee — cần truyền từ nơi gọi makeCall
        const remoteId = calleeUserId || calleeUsername; // fallback về username
        calleeIdRef.current = remoteId;

        const pc = createPeerConnection(remoteId, isVid);
        stream.getTracks().forEach((t) => pc.addTrack(t, stream));

        const callId = crypto.randomUUID();
        currentCallIdRef.current = callId;

        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);

        setCurrentCall({ callId, callee: calleeUsername, isVideoCall: isVid });
        setCallStatus("Đang đổ chuông...");

        sendSignal(remoteId, "offer", JSON.stringify({
            sdp: offer,
            isVideoCall: isVid,
            callerName: myUsernameRef.current,
            callerAvatar: null,
            callId,
        }));
    }, [createMediaStream, createPeerConnection, sendSignal, assignStreamToVideo]);

    // ── Accept incoming call ─────────────────────────────────────────
    const acceptCall = useCallback(async () => {
        if (!incomingCaller) return;
        const { sdp, fromUserId, isVideoCall: isVid } = incomingCaller;
        stopSound();
        setIsVideoCall(!!isVid);

        const stream = await createMediaStream(!!isVid);
        if (!stream) return;
        localStreamRef.current = stream;
        assignStreamToVideo(localVideoElRef, stream);

        const remoteId = fromUserId;
        calleeIdRef.current = remoteId;

        const pc = createPeerConnection(remoteId, !!isVid);
        stream.getTracks().forEach((t) => pc.addTrack(t, stream));

        await pc.setRemoteDescription(new RTCSessionDescription(sdp));
        remoteDescSetRef.current = true;

        // Flush pending ICE candidates
        for (const c of pendingCandidatesRef.current) {
            await pc.addIceCandidate(new RTCIceCandidate(c));
        }
        pendingCandidatesRef.current = [];

        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);

        sendSignal(remoteId, "answer", JSON.stringify({ sdp: answer }));

        setIncomingCaller(null);
        setCurrentCall({ callId: null, callee: fromUserId, isVideoCall: !!isVid });
        setCallStatus("Đang kết nối...");
        isOfferPendingRef.current = false;
    }, [incomingCaller, createMediaStream, createPeerConnection, sendSignal, assignStreamToVideo]);

    // ── Reject incoming call ─────────────────────────────────────────
    const rejectCall = useCallback(() => {
        stopSound();
        if (incomingCaller?.fromUserId) {
            sendSignal(incomingCaller.fromUserId, "reject", "{}");
        }
        cleanupCall("rejected");
    }, [incomingCaller, sendSignal, cleanupCall]);

    // ── End ongoing call ─────────────────────────────────────────────
    const endCall = useCallback(() => {
        const remoteId = calleeIdRef.current;
        if (remoteId) {
            sendSignal(remoteId, "end", "{}");
        }
        setIsCallEnding(true);
        setTimeout(() => cleanupCall("hangup"), 1500);
    }, [sendSignal, cleanupCall]);

    // ── Toggle mute ──────────────────────────────────────────────────
    const toggleMute = useCallback((muted) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        stream.getAudioTracks().forEach((t) => (t.enabled = !muted));
    }, []);

    // ── Toggle camera ────────────────────────────────────────────────
    const toggleLocalVideo = useCallback((enabled) => {
        const stream = localStreamRef.current;
        if (!stream) return;
        stream.getVideoTracks().forEach((t) => (t.enabled = !enabled));
    }, []);

    return (
        <CallContext.Provider
            value={{
                isConnected,
                currentCall,
                callStatus,
                incomingCaller,
                isCallEnding,
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