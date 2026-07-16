package org.example.api.model;

public class ShortenUrlRequest {

    private String url;
    private String alias;

    public ShortenUrlRequest() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
