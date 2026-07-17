package org.example.integrationTests;

import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class IntegrationTestSupport {

    protected static final String BASE_URL = normalizeBaseUrl(
            System.getProperty(
                    "it.baseUrl",
                    "http://localhost:8080/url-shortener"
            )
    );

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.config = RestAssuredConfig.config()
                .redirect(
                        RedirectConfig.redirectConfig()
                                .followRedirects(false)
                );

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected RequestSpecification request() {
        return given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    protected Response createUrl(String targetUrl, String alias) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("url", targetUrl);

        if (alias != null) {
            body.put("alias", alias);
        }

        return request()
                .body(body)
                .post("/urls");
    }

    protected void deleteQuietly(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return;
        }

        try {
            request().delete("/urls/{alias}", alias);
        } catch (RuntimeException ignored) {
            // Limpeza de melhor esforço: não deve esconder a falha principal.
        }
    }

    protected static String uniqueAlias(String prefix) {
        return prefix + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    private static String normalizeBaseUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("A propriedade it.baseUrl está vazia");
        }

        String normalized = value.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
