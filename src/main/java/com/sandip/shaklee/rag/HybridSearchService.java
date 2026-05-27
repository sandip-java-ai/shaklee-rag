package com.sandip.shaklee.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    private final List<Product> productCache;

    public HybridSearchService(List<Product> productCache) {
        this.productCache = productCache;
    }

    // Detect if question needs price-based search
    public boolean isPriceQuery(String question) {
        String q = question.toLowerCase();
        return q.contains("cheapest") || q.contains("lowest price") ||
                q.contains("most expensive") || q.contains("highest price") ||
                q.contains("affordable") || q.contains("under $") ||
                q.contains("less than $") || q.contains("price range") ||
                q.contains("budget");
    }

    // Handle price-based queries with exact sorting
    public String handlePriceQuery(String question) {
        if (productCache.isEmpty()) {
            return "Product catalog not loaded. Please run ingestion first.";
        }

        String q = question.toLowerCase();
        log.info("Handling price query: {}", question);

        // Cheapest for members
        if (q.contains("cheapest") || q.contains("lowest") ||
                q.contains("affordable") || q.contains("budget")) {

            String priceType = q.contains("guest") ? "guest" :
                    q.contains("distributor") ? "distributor" : "member";

            List<Product> sorted = productCache.stream()
                    .filter(p -> getPrice(p, priceType) > 0)
                    .sorted(Comparator.comparingDouble(p -> getPrice(p, priceType)))
                    .limit(5)
                    .collect(Collectors.toList());

            return formatPriceResults(sorted, "cheapest", priceType);
        }

        // Most expensive
        if (q.contains("most expensive") || q.contains("highest")) {
            String priceType = q.contains("guest") ? "guest" :
                    q.contains("distributor") ? "distributor" : "member";

            List<Product> sorted = productCache.stream()
                    .filter(p -> getPrice(p, priceType) > 0)
                    .sorted(Comparator.comparingDouble(
                            (Product p) -> getPrice(p, priceType)).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            return formatPriceResults(sorted, "most expensive", priceType);
        }

        // Under a specific price
        if (q.contains("under $") || q.contains("less than $")) {
            double maxPrice = extractPrice(question);
            if (maxPrice > 0) {
                List<Product> filtered = productCache.stream()
                        .filter(p -> p.getMemberPrice() > 0 && p.getMemberPrice() < maxPrice)
                        .sorted(Comparator.comparingDouble(Product::getMemberPrice))
                        .limit(10)
                        .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    return "No products found under $" + maxPrice + " for members.";
                }
                return formatPriceResults(filtered, "under $" + maxPrice, "member");
            }
        }

        return null; // fall back to RAG
    }

    private double getPrice(Product p, String type) {
        return switch (type) {
            case "guest"       -> p.getGuestPrice();
            case "distributor" -> p.getDistributorPrice();
            default            -> p.getMemberPrice();
        };
    }

    private String formatPriceResults(List<Product> products,
                                      String label, String priceType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the ").append(label)
                .append(" products for ").append(priceType).append(" pricing:\n\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(i + 1).append(". **").append(p.getName()).append("**\n");
            sb.append("   Price: $").append(String.format("%.2f", getPrice(p, priceType))).append("\n");
            if (p.getSummary() != null) {
                sb.append("   ").append(p.getSummary()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private double extractPrice(String question) {
        try {
            String[] parts = question.split("\\$");
            if (parts.length > 1) {
                String priceStr = parts[1].replaceAll("[^0-9.]", "");
                return Double.parseDouble(priceStr);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}