package org.example.integrationTests;

import io.restassured.response.Response;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertNotNull;

public class UrlLifecycleEndpointIT extends IntegrationTestSupport {

    @Test
    public void shouldCreateUrlWithAliasAndRedirect() {
        String alias = uniqueAlias("create");
        String targetUrl = "https://example.com/products/123";

        try {
            Response creation = createUrl(targetUrl, alias);

            creation.then()
                    .statusCode(201)
                    .header("Location", endsWith("/" + alias))
                    .body("alias", equalTo(alias))
                    .body("url", equalTo(targetUrl))
                    .body("shortUrl", endsWith("/" + alias));

            String createdAt = creation.jsonPath().getString("createdAt");
            assertNotNull(createdAt);
            Instant.parse(createdAt);

            request()
                    .get("/{alias}", alias)
                    .then()
                    .statusCode(302)
                    .header("Location", equalTo(targetUrl));
        } finally {
            deleteQuietly(alias);
        }
    }

    @Test
    public void shouldGenerateAliasWhenItIsNotProvided() {
        String generatedAlias = null;
        String targetUrl = "https://example.com/generated";

        try {
            Response creation = createUrl(targetUrl, null);

            creation.then()
                    .statusCode(201)
                    .body("url", equalTo(targetUrl))
                    .body("alias", matchesPattern("[A-Za-z0-9_-]{3,32}"));

            generatedAlias = creation.jsonPath().getString("alias");

            request()
                    .get("/{alias}", generatedAlias)
                    .then()
                    .statusCode(302)
                    .header("Location", equalTo(targetUrl));
        } finally {
            deleteQuietly(generatedAlias);
        }
    }

    @Test
    public void shouldListCreatedUrls() {
        String alias = uniqueAlias("list");
        String targetUrl = "https://example.com/listed";

        try {
            createUrl(targetUrl, alias)
                    .then()
                    .statusCode(201);

            request()
                    .get("/urls")
                    .then()
                    .statusCode(200)
                    .body("alias", hasItem(alias))
                    .body("url", hasItem(targetUrl));
        } finally {
            deleteQuietly(alias);
        }
    }

    @Test
    public void shouldUpdateTargetWithoutChangingCreationTimestamp() {
        String alias = uniqueAlias("update");
        String originalUrl = "https://example.com/original";
        String updatedUrl = "https://example.com/updated";

        try {
            Response creation = createUrl(originalUrl, alias);
            creation.then().statusCode(201);

            String createdAt = creation.jsonPath().getString("createdAt");

            request()
                    .body(Collections.singletonMap("url", updatedUrl))
                    .put("/urls/{alias}", alias)
                    .then()
                    .statusCode(200)
                    .body("alias", equalTo(alias))
                    .body("url", equalTo(updatedUrl))
                    .body("createdAt", equalTo(createdAt));

            request()
                    .get("/{alias}", alias)
                    .then()
                    .statusCode(302)
                    .header("Location", equalTo(updatedUrl));
        } finally {
            deleteQuietly(alias);
        }
    }

    @Test
    public void shouldDeleteUrlAndReturnNotFoundAfterwards() {
        String alias = uniqueAlias("delete");

        createUrl("https://example.com/to-delete", alias)
                .then()
                .statusCode(201);

        request()
                .delete("/urls/{alias}", alias)
                .then()
                .statusCode(204);

        request()
                .get("/{alias}", alias)
                .then()
                .statusCode(404);
    }
}
