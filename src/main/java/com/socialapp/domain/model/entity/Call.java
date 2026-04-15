package com.socialapp.domain.model.entity;

import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;

import java.time.LocalDateTime;

/**
 * Entity: Call — kế thừa Message (Subclass of theo graph)
 * Đại diện cho một cuộc gọi thoại / video trong Chat.
 */
public class Call extends Message {

    private String        callId;       // mã từ API thứ 3 (Agora/Twilio…)
    private LocalDateTime callAt;
    private LocalDateTime endAt;
    private boolean       isAnswered;
    private boolean       isEnded;
    private boolean       isVideoCall;

    public Call(String id, UserId senderId, boolean isVideoCall) {
        super(id, senderId, new MessageContent(""));
        this.isAnswered  = false;
        this.isEnded     = false;
        this.isVideoCall = isVideoCall;
    }

    // ── Business methods ─────────────────────────────────────

    public void setThirdPartyCallId(String callId) {
        this.callId = callId;
    }

    public void answer() {
        if (isEnded) throw new IllegalStateException("Call already ended");
        this.isAnswered = true;
        this.callAt     = LocalDateTime.now();
    }

    public void end() {
        this.isEnded = true;
        this.endAt   = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────

    public String        getCallId()     { return callId; }
    public LocalDateTime getCallAt()     { return callAt; }
    public LocalDateTime getEndAt()      { return endAt; }
    public boolean       isAnswered()    { return isAnswered; }
    public boolean       isEnded()       { return isEnded; }
    public boolean       isVideoCall()   { return isVideoCall; }
}