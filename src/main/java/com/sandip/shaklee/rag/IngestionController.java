package com.sandip.shaklee.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest() {
        int count = ingestionService.ingestProducts();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "productsIngested", count,
                "message", count + " products embedded and stored"
        ));
    }
}