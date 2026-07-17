package org.example.integrationTests;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.notNullValue;

public class UrlValidationEndpointIT extends IntegrationTestSupport {

    @Test
    public void shouldRejectRequestWithoutUrl() {
        request()
                .body(new HashMap<String, Object>())
                .post("/urls")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    public void shouldRejectUnsupportedUrlScheme() {
        request()
                .body(Collections.singletonMap("url", "ftp://example.com/file"))
                .post("/urls")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    public void shouldRejectInvalidAlias() {
        String body = "{\"url\":\"https://example.com\",\"alias\":\"x\"}";

        request()
                .body(body)
                .post("/urls")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    public void shouldRejectDuplicatedAlias() {
        String alias = uniqueAlias("duplicate");

        try {
            createUrl("https://example.com/first", alias)
                    .then()
                    .statusCode(201);

            createUrl("https://example.com/second", alias)
                    .then()
                    .statusCode(409)
                    .body("error", notNullValue());
        } finally {
            deleteQuietly(alias);
        }
    }

    @Test
    public void shouldReturnNotFoundWhenUpdatingUnknownAlias() {
        String alias = uniqueAlias("missing-upd");

        request()
                .body(Collections.singletonMap("url", "https://example.com/new"))
                .put("/urls/{alias}", alias)
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingUnknownAlias() {
        String alias = uniqueAlias("missing-del");

        request()
                .delete("/urls/{alias}", alias)
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }
}
