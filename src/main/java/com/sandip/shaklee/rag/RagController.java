package com.sandip.shaklee.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> request) {

        String question = request.get("question");
        String sessionId = request.get("sessionId");

        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "question is required"));
        }

        // Generate session ID if not provided
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        String answer = ragService.query(question, sessionId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", sessionId,
                "question", question,
                "answer", answer
        ));
    }
}