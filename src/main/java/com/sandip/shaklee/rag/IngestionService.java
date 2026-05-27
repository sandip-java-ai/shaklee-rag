package com.sandip.shaklee.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final ProductXmlParser productXmlParser;
    private final List<Product> productCache;

    public IngestionService(VectorStore vectorStore, ProductXmlParser productXmlParser, List<Product> productCache) {
        this.vectorStore = vectorStore;
        this.productXmlParser = productXmlParser;
        this.productCache = productCache;
    }

    public int ingestProducts() {
        log.info("Starting product ingestion...");

        List<Product> products = productXmlParser.fetchProducts();

        if (products.isEmpty()) {
            log.warn("No products to ingest");
            return 0;
        }

        // Populate cache for price queries
        productCache.clear();
        productCache.addAll(products);
        log.info("Product cache populated with {} products", productCache.size());

        List<Document> documents = new ArrayList<>();
        for (Product p : products) {
            // Convert product to a rich text chunk for embedding
            String content = buildProductText(p);
            // Metadata for filtering and display
            Map<String, Object> metadata = Map.of(
                    "code",        p.getCode() != null ? p.getCode() : "",
                    "name",        p.getName() != null ? p.getName() : "",
                    "memberPrice", p.getMemberPrice(),
                    "guestPrice",  p.getGuestPrice(),
                    "stockStatus", p.getStockStatus() != null ? p.getStockStatus() : "",
                    "pdpUrl",      p.getPdpUrl() != null ? p.getPdpUrl() : "",
                    "rating",      p.getAverageRating()
            );

            documents.add(new Document(content, metadata));
        }

        log.info("Adding {} documents to vector store...", documents.size());
        vectorStore.add(documents);

        // Save to file so we don't re-embed on next startup
        File vectorFile = new File("shaklee-vectors.json");
        if (vectorStore instanceof SimpleVectorStore svs) {
            svs.save(vectorFile);
            log.info("Saved embeddings to {}", vectorFile.getAbsolutePath());
        }

        log.info("Ingestion complete — {} products embedded", documents.size());
        return documents.size();
    }

    private String buildProductText(Product p) {
        return String.format("""
            Product: %s
            Code: %s
            Description: %s
            Summary: %s
            Member Price: $%.2f
            Guest Price: $%.2f
            Distributor Price: $%.2f
            Stock: %s
            Rating: %.1f/5
            URL: %s
            """,
                p.getName(),
                p.getCode(),
                p.getDescription() != null ? p.getDescription() : "",
                p.getSummary() != null ? p.getSummary() : "",
                p.getMemberPrice(),
                p.getGuestPrice(),
                p.getDistributorPrice(),
                p.getStockStatus() != null ? p.getStockStatus() : "Unknown",
                p.getAverageRating(),
                p.getPdpUrl() != null ? p.getPdpUrl() : ""
        );
    }
}