package org.example.api.model;

import java.time.Instant;

public class ShortUrl {

    private final String alias;
    private final String targetUrl;
    private final Instant createdAt;

    public ShortUrl(
            String alias,
            String targetUrl,
            Instant createdAt
    ) {
        this.alias = alias;
        this.targetUrl = targetUrl;
        this.createdAt = createdAt;
    }

    public String getAlias() {
        return alias;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
