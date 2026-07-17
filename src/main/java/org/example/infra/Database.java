package org.example.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static final String URL = System.getProperty(
            "db.url",
            "jdbc:h2:mem:urlshortener;DB_CLOSE_DELAY=-1"
    );

    private static final String USER =
            System.getProperty("db.user", "sa");

    private static final String PASSWORD =
            System.getProperty("db.password", "");

    private Database() {
    }

    public static void initialize() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "Driver do H2 não encontrado",
                    exception
            );
        }

        String sql =
                "CREATE TABLE IF NOT EXISTS SHORT_URLS (" +
                        "SHORT_CODE VARCHAR(32) PRIMARY KEY, " +
                        "TARGET_URL VARCHAR(2048) NOT NULL, " +
                        "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                        ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível inicializar o banco", exception);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
