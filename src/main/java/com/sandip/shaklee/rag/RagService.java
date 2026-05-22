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

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public String query(String userQuestion) {
        log.info("RAG query: {}", userQuestion);

        // Step 1 — Find relevant products from vector store
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

        // Step 2 — Build context from retrieved products
        String context = relevant.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // Step 3 — Send context + question to Claude/OpenAI
        String prompt = """
            You are a helpful Shaklee product assistant.
            Answer the user's question based ONLY on the product information provided below.
            If the answer is not in the provided information, say so clearly.
            Be concise and helpful.
            
            PRODUCT INFORMATION:
            %s
            
            USER QUESTION: %s
            """.formatted(context, userQuestion);

        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("RAG answer generated");
        return answer;
    }
}