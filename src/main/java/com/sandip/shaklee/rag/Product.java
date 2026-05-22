package com.sandip.shaklee.rag;

public class Product {
    private String code;
    private String name;
    private String baseProductName;
    private String description;
    private String summary;
    private String pdpUrl;
    private double memberPrice;
    private double guestPrice;
    private double distributorPrice;
    private String stockStatus;
    private double averageRating;

    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseProductName() { return baseProductName; }
    public void setBaseProductName(String baseProductName) { this.baseProductName = baseProductName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getPdpUrl() { return pdpUrl; }
    public void setPdpUrl(String pdpUrl) { this.pdpUrl = pdpUrl; }

    public double getMemberPrice() { return memberPrice; }
    public void setMemberPrice(double memberPrice) { this.memberPrice = memberPrice; }

    public double getGuestPrice() { return guestPrice; }
    public void setGuestPrice(double guestPrice) { this.guestPrice = guestPrice; }

    public double getDistributorPrice() { return distributorPrice; }
    public void setDistributorPrice(double distributorPrice) { this.distributorPrice = distributorPrice; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}