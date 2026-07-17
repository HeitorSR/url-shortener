package org.example.integrationTests;

import org.junit.Assume;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.request;
import static org.junit.Assert.assertTrue;

public class ExpiredUrlsEndpointIT extends IntegrationTestSupport {

    @Test
    public void shouldReturnExpirationCleanupSummary() {
        int deleted = request()
                .delete("/urls/expired")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("deleted");

        assertTrue("A quantidade excluída não pode ser negativa", deleted >= 0);
    }

    @Test
    public void shouldDeleteExpiredUrlAndKeepActiveUrl() throws Exception {
        String databaseUrl = System.getProperty("it.db.url", "").trim();

        Assume.assumeTrue(
                "Defina it.db.url para executar o cenário completo de expiração",
                !databaseUrl.isEmpty()
        );

        long expirationHours = Long.parseLong(
                System.getProperty("it.expiration.hours", "168")
        );

        String expiredAlias = uniqueAlias("expired");
        String activeAlias = uniqueAlias("active");

        try {
            insertExpiredUrl(
                    databaseUrl,
                    expiredAlias,
                    "https://example.com/expired",
                    Instant.now().minus(
                            expirationHours + 1,
                            ChronoUnit.HOURS
                    )
            );

            createUrl("https://example.com/active", activeAlias)
                    .then()
                    .statusCode(201);

            int deleted = request()
                    .delete("/urls/expired")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getInt("deleted");

            assertTrue("Ao menos a URL expirada deveria ser excluída", deleted >= 1);

            request()
                    .get("/{alias}", expiredAlias)
                    .then()
                    .statusCode(404);

            request()
                    .get("/{alias}", activeAlias)
                    .then()
                    .statusCode(302);
        } finally {
            deleteQuietly(activeAlias);
            deleteDirectly(databaseUrl, expiredAlias);
        }
    }

    private void insertExpiredUrl(
            String databaseUrl,
            String alias,
            String targetUrl,
            Instant createdAt
    ) throws Exception {
        Class.forName("org.h2.Driver");

        String sql =
                "INSERT INTO SHORT_URLS " +
                "(SHORT_CODE, TARGET_URL, CREATED_AT) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = openConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, alias);
            statement.setString(2, targetUrl);
            statement.setTimestamp(3, Timestamp.from(createdAt));
            statement.executeUpdate();
        }
    }

    private void deleteDirectly(String databaseUrl, String alias) {
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            return;
        }

        try (Connection connection = openConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM SHORT_URLS WHERE SHORT_CODE = ?"
             )) {

            statement.setString(1, alias);
            statement.executeUpdate();
        } catch (SQLException ignored) {
            // Limpeza de melhor esforço.
        }
    }

    private Connection openConnection(String databaseUrl) throws SQLException {
        return DriverManager.getConnection(
                databaseUrl,
                System.getProperty("it.db.user", "sa"),
                System.getProperty("it.db.password", "")
        );
    }
}
