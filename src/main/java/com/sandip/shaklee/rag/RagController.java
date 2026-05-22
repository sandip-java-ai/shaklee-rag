package com.sandip.shaklee.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "question is required"));
        }

        String answer = ragService.query(question);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "question", question,
                "answer", answer
        ));
    }
}