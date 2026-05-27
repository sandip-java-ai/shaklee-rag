package com.sandip.shaklee.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final HybridSearchService hybridSearchService;
    private final ConversationMemory conversationMemory;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder,
                      HybridSearchService hybridSearchService, ConversationMemory conversationMemory) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.hybridSearchService = hybridSearchService;
        this.conversationMemory = conversationMemory;
    }

    public String query(String userQuestion, String sessionId) {
        log.info("RAG query: {}", userQuestion);

        // Build history text first
        List<ConversationMemory.Message> history = conversationMemory.getHistory(sessionId);

        StringBuilder historyText = new StringBuilder();
        for (ConversationMemory.Message msg : history) {
            historyText.append(msg.role().toUpperCase())
                    .append(": ")
                    .append(msg.content())
                    .append("\n");
        }

        // Step 1 — Check price query
        if (hybridSearchService.isPriceQuery(userQuestion)) {
            log.info("Routing to hybrid price search");
            String priceResult = hybridSearchService.handlePriceQuery(userQuestion,
                    historyText.toString());
            if (priceResult != null) {
                conversationMemory.addMessage(sessionId, "user", userQuestion);
                conversationMemory.addMessage(sessionId, "assistant", priceResult);
                return priceResult;
            }
        }

        // Step 2 — Semantic RAG search
        List<Document> relevant = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuestion)
                        .topK(5)
                        .build()
        );

        log.info("Found {} relevant products", relevant.size());

        if (relevant.isEmpty()) {
            return "I couldn't find any relevant products for your question.";
        }

        // Step 3 — Build context
        String context = relevant.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // Step 5 — Build prompt with history
        String prompt = """
            You are a helpful Shaklee product assistant.
            Answer based ONLY on the product information provided.
            If not in the provided information, say so clearly.
            
            PRODUCT INFORMATION:
            %s
            
            CONVERSATION HISTORY:
            %s
            
            CURRENT QUESTION: %s
            """.formatted(
                context,
                historyText.toString(),
                userQuestion
        );

        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Step 6 — Save to memory
        conversationMemory.addMessage(sessionId, "user", userQuestion);
        conversationMemory.addMessage(sessionId, "assistant", answer);

        log.info("RAG answer generated");
        return answer;
    }
}