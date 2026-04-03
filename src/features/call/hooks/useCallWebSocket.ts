import { useEffect, useRef } from "react";
import { websocketService } from "@services/websocket.service";
import { useCallStore } from "../store/call.store";
import { useSessionStore } from "@stores/session.store";
import { CALL_WEBSOCKET_EVENTS } from "../constants/call.constants";
import type {
  IncomingCallPayload,
  CallEndedPayload,
} from "../types/call.types";
import toast from "react-hot-toast";

export const useCallWebSocket = () => {
  // Backend JWT filter sets accountId as principal name → convertAndSendToUser dùng accountId
  // Nên phải subscribe theo accountId, KHÔNG phải userId
  const accountId = useSessionStore((state) => state.accountId);
  const userId    = useSessionStore((state) => state.userId);
  const { setIncomingCall, setCallEnded, clearSession } = useCallStore();

  const sessionRef = useRef(useCallStore.getState().session);
  useEffect(() => {
    return useCallStore.subscribe((state) => {
      sessionRef.current = state.session;
    });
  }, []);

  useEffect(() => {
    // Cần accountId để subscribe đúng topic WebSocket
    if (!accountId || !userId) return;

    console.log("[WS] accountId (principal):", accountId);
    console.log("[WS] userId:", userId);

    // Backend publishToUser(accountId, "incoming_call", ...) →
    // Spring convertAndSendToUser(accountId, "/queue/incoming_call", ...)
    // → broker delivers to /user/{accountId}/queue/incoming_call
    const incomingTopic = `/user/${accountId}/queue/${CALL_WEBSOCKET_EVENTS.INCOMING_CALL}`;
    const endedTopic    = `/user/${accountId}/queue/${CALL_WEBSOCKET_EVENTS.CALL_ENDED}`;

    websocketService.subscribe(incomingTopic, (frame) => {
      console.log("[WS] incoming_call frame received:", frame.body);
      const payload: IncomingCallPayload = JSON.parse(frame.body);
      console.log("[WS] parsed payload:", payload);
      const session = sessionRef.current;

      // Busy guard: đang có call thì thông báo bận
      if (
        session?.status === "connected" ||
        session?.status === "outgoing"  ||
        session?.status === "incoming"
      ) {
        toast(`${payload.callerName} đang gọi nhưng bạn đang bận`, {
          icon: "📵",
        });
        return;
      }

      // userId là identity của current user để điền vào CallSession.receiverId
      setIncomingCall(payload, userId, "");
      console.log(
        "[WS] setIncomingCall called, store:",
        useCallStore.getState().session,
      );
    });

    websocketService.subscribe(endedTopic, (frame) => {
      const payload: CallEndedPayload = JSON.parse(frame.body);
      const session = sessionRef.current;

      if (session?.callId === payload.callId) {
        if (session.status === "outgoing") {
          toast("Không có ai bắt máy", { icon: "📵" });
        }
        setCallEnded();
        setTimeout(clearSession, 2000);
      }
    });

    return () => {
      websocketService.unsubscribe(incomingTopic);
      websocketService.unsubscribe(endedTopic);
    };
  }, [accountId, userId, setIncomingCall, setCallEnded, clearSession]);
};
