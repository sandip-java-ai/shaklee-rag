package com.sandip.shaklee.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    // In-memory product cache for price queries
    private final List<Product> productCache = new ArrayList<>();

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        // Load existing embeddings from file if available
        File vectorFile = new File("shaklee-vectors.json");
        if (vectorFile.exists()) {
            store.load(vectorFile);
        }

        return store;
    }

    @Bean
    public List<Product> productCache() {
        return productCache;
    }
}