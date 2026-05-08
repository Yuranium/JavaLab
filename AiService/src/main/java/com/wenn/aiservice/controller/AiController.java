package com.wenn.aiservice.controller;

import com.wenn.aiservice.models.dto.ChatDto;
import com.wenn.aiservice.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> chatStream(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatDto request) {
        Flux<String> response = aiService.streamChat(jwt.getSubject(), request.message());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> getHistory(@PathVariable("id") String userId) {
        return ResponseEntity.ok(aiService.getHistory(userId));
    }

    @DeleteMapping("/history/{id}/clear")
    public ResponseEntity<?> clearHistory(@PathVariable("id") String userId) {
        aiService.clearHistory(userId);
        return ResponseEntity.ok(
                java.util.Map.of("status", "cleared", "userId", userId)
        );
    }
}