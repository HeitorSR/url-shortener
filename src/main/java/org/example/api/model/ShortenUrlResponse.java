package org.example.api.model;

public class ShortenUrlResponse {


    private final String alias;
    private final String url;
    private final String shortUrl;
    private final String createdAt;

    public ShortenUrlResponse(
            String alias,
            String url,
            String shortUrl,
            String createdAt
    ) {
        this.alias = alias;
        this.url = url;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
    }

    public String getAlias() {
        return alias;
    }

    public String getUrl() {
        return url;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
