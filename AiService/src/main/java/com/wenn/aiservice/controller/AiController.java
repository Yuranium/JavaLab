package com.wenn.aiservice.controller;

import com.wenn.aiservice.models.dto.ChatDto;
import com.wenn.aiservice.service.ChatService;
import com.wenn.aiservice.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final ChatService chatService;
    private final HistoryService historyService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatDto request) {
        String response = chatService.chat(request.userId(), request.message());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/stream")
    public ResponseEntity<Flux<String>> chatStream(@RequestBody ChatDto request) {
        Flux<String> response = chatService.streamChat(request.userId(), request.message());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/history/{id}")
    public ResponseEntity<?> getHistory(@PathVariable("id") String userId) {
        return ResponseEntity.ok(historyService.getHistory(userId));
    }

    @DeleteMapping("/history/{id}/clear")
    public ResponseEntity<?> clearHistory(@PathVariable("id") String userId) {
        historyService.clearHistory(userId);
        return ResponseEntity.ok(
                java.util.Map.of("status", "cleared", "userId", userId)
        );
    }
}
