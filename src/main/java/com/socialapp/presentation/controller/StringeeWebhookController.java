package com.socialapp.presentation.controller;

import com.socialapp.application.call.dto.CallDtos.*;
import com.socialapp.application.call.usecase.HandleStringeeAnswerUseCase;
import com.socialapp.application.call.usecase.HandleStringeeEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller nhận webhook từ Stringee server.
 *
 * Endpoints:
 *   GET  /api/stringee/answer  → Stringee answer URL (SCCO response)
 *   POST /api/stringee/event   → Stringee call event (started/answered/ended/...)
 *
 * Các endpoint này KHÔNG cần JWT — Stringee server gọi trực tiếp.
 * Phải được permit trong SecurityConfig.
 */
@Slf4j
@RestController
@RequestMapping("/api/stringee")
@RequiredArgsConstructor
public class StringeeWebhookController {

    private final HandleStringeeAnswerUseCase handleStringeeAnswerUseCase;
    private final HandleStringeeEventUseCase  handleStringeeEventUseCase;

    /**
     * GET /api/stringee/answer
     * Stringee gọi URL này ngay khi cuộc gọi bắt đầu để nhận SCCO instructions.
     */
    @GetMapping("/answer")
    public ResponseEntity<List<StringeeSccoResponse>> handleAnswer(
            @RequestParam(defaultValue = "false") String appToPhone,
            @RequestParam(defaultValue = "60")    int    timeout,
            @RequestParam(defaultValue = "-1")    int    maxConnectTime,
            @RequestParam(defaultValue = "true")  boolean peerToPeerCall,
            @RequestParam(defaultValue = "false", name = "record") boolean isRecord,
            @RequestParam(defaultValue = "mp3")   String recordFormat,
            @RequestParam boolean  fromInternal,
            @RequestParam(name = "from") String fromId,
            @RequestParam(name = "to")   String toId,
            @RequestParam String  projectId,
            @RequestParam String  callId,
            @RequestParam(defaultValue = "false") boolean videocall) {

        List<StringeeSccoResponse> scco = handleStringeeAnswerUseCase.execute(
                fromId, toId, callId,
                fromInternal,
                "true".equalsIgnoreCase(appToPhone),
                timeout, maxConnectTime, peerToPeerCall,
                isRecord, recordFormat,
                videocall
        );
        return ResponseEntity.ok(scco);
    }

    /**
     * POST /api/stringee/event
     * Stringee gửi event này sau mỗi thay đổi trạng thái cuộc gọi.
     */
    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> handleEvent(
            @RequestBody StringeeCallEvent event) {
        log.debug("Stringee event received: type={}, call_id={}, status={}",
                event.type(), event.call_id(), event.call_status());
        Map<String, String> result = handleStringeeEventUseCase.execute(event);
        return ResponseEntity.ok(result);
    }
}
