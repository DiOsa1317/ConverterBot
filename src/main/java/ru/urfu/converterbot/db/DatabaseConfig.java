package ru.urfu.converterbot.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *  Конфигурация подключения к встроенной БД H2.
 * */
public class DatabaseConfig {
    private static final String JDBC_URL = "jdbc:h2:./converterbot";
    private static final String JDBC_USER = "converter";
    private static final String JDBC_PASSWORD = "";

    /** Подключается к базе данных */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    /**
     * Создает таблицы на основе SQL-схемы.
     */
    public void initSchema() throws SQLException {
        String schema = loadSchema();

        try (var connection = getConnection();
             var statement = connection.createStatement()) {

            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private String loadSchema() throws SQLException {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("db/schema.sql")) {

            if (input == null) {
                throw new SQLException("Файл db/schema.sql не найден");
            }

            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Не удалось прочитать SQL-схему", e);
        }
    }
}
