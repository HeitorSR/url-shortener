package org.example.api;

import org.example.api.model.ShortUrl;
import org.example.infra.Database;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jdk.nashorn.internal.objects.NativeArray.map;

public class UrlRepository {

    public boolean insert(String alias, String targetUrl, Instant createdAt)
            throws SQLException {

        String sql =
                "INSERT INTO SHORT_URLS " +
                        "(SHORT_CODE, TARGET_URL, CREATED_AT) " +
                        "VALUES (?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, alias);
            statement.setString(2, targetUrl);
            statement.setTimestamp(
                    3,
                    Timestamp.from(createdAt)
            );
            statement.executeUpdate();

            return true;

        } catch (SQLException exception) {
            // 23505: violação de chave única
            if ("23505".equals(exception.getSQLState())) {
                return false;
            }

            throw exception;
        }
    }

    public Optional<String> findTargetUrl(String alias)
            throws SQLException {

        String sql =
                "SELECT TARGET_URL FROM SHORT_URLS " +
                        "WHERE SHORT_CODE = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, alias);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(
                            resultSet.getString("TARGET_URL")
                    );
                }

                return Optional.empty();
            }
        }
    }

    public List<ShortUrl> findAll() throws SQLException {
        String sql =
                "SELECT SHORT_CODE, TARGET_URL, CREATED_AT " +
                        "FROM SHORT_URLS " +
                        "ORDER BY CREATED_AT DESC";

        List<ShortUrl> urls = new ArrayList<ShortUrl>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                urls.add(map(resultSet));
            }
        }

        return urls;
    }

    public boolean updateTargetUrl(
            String alias,
            String targetUrl
    ) throws SQLException {

        String sql =
                "UPDATE SHORT_URLS " +
                        "SET TARGET_URL = ? " +
                        "WHERE SHORT_CODE = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, targetUrl);
            statement.setString(2, alias);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteByAlias(String alias)
            throws SQLException {

        String sql =
                "DELETE FROM SHORT_URLS " +
                        "WHERE SHORT_CODE = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, alias);

            return statement.executeUpdate() > 0;
        }
    }

    public int deleteCreatedBefore(Instant threshold)
            throws SQLException {

        String sql =
                "DELETE FROM SHORT_URLS " +
                        "WHERE CREATED_AT < ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.from(threshold)
            );

            return statement.executeUpdate();
        }
    }

    private ShortUrl map(ResultSet resultSet)
            throws SQLException {

        return new ShortUrl(
                resultSet.getString("SHORT_CODE"),
                resultSet.getString("TARGET_URL"),
                resultSet.getTimestamp("CREATED_AT").toInstant()
        );
    }

    public Optional<ShortUrl> findByAlias(String alias)
            throws SQLException {

        String sql =
                "SELECT SHORT_CODE, TARGET_URL, CREATED_AT " +
                        "FROM SHORT_URLS " +
                        "WHERE SHORT_CODE = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, alias);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(resultSet));
            }
        }
    }
}
