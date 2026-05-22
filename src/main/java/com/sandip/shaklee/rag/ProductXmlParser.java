package com.sandip.shaklee.rag;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductXmlParser {

    private static final Logger log = LoggerFactory.getLogger(ProductXmlParser.class);

    private static final String FEED_URL =
            "https://storage.googleapis.com/fb-product-data-feed-prod/sf-rss-us-products-list.xml";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final XmlMapper xmlMapper;

    public ProductXmlParser() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Product> fetchProducts() {
        log.info("Fetching product feed from GCP...");
        try {
            Request request = new Request.Builder().url(FEED_URL).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Failed to fetch feed: {}", response.code());
                    return List.of();
                }

                String xml = response.body().string();
                log.info("Feed fetched successfully — {} bytes", xml.length());

                // Parse XML into map
                Map<String, Object> parsed = xmlMapper.readValue(xml, Map.class);

                // Extract products list
                Object productsObj = parsed.get("products");
                if (productsObj == null) {
                    log.warn("No products found in feed");
                    return List.of();
                }

                List<Map<String, Object>> productMaps;
                if (productsObj instanceof List) {
                    productMaps = (List<Map<String, Object>>) productsObj;
                } else {
                    productMaps = List.of((Map<String, Object>) productsObj);
                }

                List<Product> products = new ArrayList<>();
                for (Map<String, Object> pm : productMaps) {
                    try {
                        Product p = mapToProduct(pm);
                        if (p != null) products.add(p);
                    } catch (Exception e) {
                        log.warn("Skipping product due to parse error: {}", e.getMessage());
                    }
                }

                log.info("Parsed {} products", products.size());
                return products;
            }

        } catch (Exception e) {
            log.error("Error fetching product feed", e);
            return List.of();
        }
    }

    private Product mapToProduct(Map<String, Object> pm) {
        Product p = new Product();

        p.setCode(str(pm, "code"));
        p.setName(str(pm, "name"));
        p.setBaseProductName(str(pm, "baseProductName"));
        p.setSummary(str(pm, "summary"));
        p.setPdpUrl(str(pm, "pdp_url"));

        // Strip HTML from description
        String desc = str(pm, "description");
        if (desc != null) {
            desc = desc.replaceAll("<[^>]+>", "").trim();
        }
        p.setDescription(desc);

        // Stock status
        Object stockObj = pm.get("stock");
        if (stockObj instanceof Map) {
            p.setStockStatus(str((Map<String, Object>) stockObj, "stockLevelStatus"));
        }

        // Average rating
        try {
            String rating = str(pm, "averageRating");
            if (rating != null) p.setAverageRating(Double.parseDouble(rating));
        } catch (Exception ignored) {}

        // Prices — can be single map or list of maps
        Object pricesObj = pm.get("prices");
        if (pricesObj instanceof List) {
            for (Object priceItem : (List<?>) pricesObj) {
                if (priceItem instanceof Map) {
                    extractPrice(p, (Map<String, Object>) priceItem);
                }
            }
        } else if (pricesObj instanceof Map) {
            extractPrice(p, (Map<String, Object>) pricesObj);
        }

        return p;
    }

    private void extractPrice(Product p, Map<String, Object> priceMap) {
        String tier = str(priceMap, "priceTier");
        String valueStr = str(priceMap, "value");
        if (tier == null || valueStr == null) return;
        try {
            double value = Double.parseDouble(valueStr);
            switch (tier) {
                case "MP"  -> p.setMemberPrice(value);
                case "SN"  -> p.setGuestPrice(value);
                case "DP"  -> p.setDistributorPrice(value);
            }
        } catch (Exception ignored) {}
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}