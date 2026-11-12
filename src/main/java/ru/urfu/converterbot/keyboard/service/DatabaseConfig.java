package ru.urfu.converterbot.keyboard.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *  Конфигурация подключения к встроенной БД H2.
 *  Хранит кастомные единицы измерения и кэш ресурсов валют (потом будет ещё история)
 * */
public class DatabaseConfig {
    private static final String JDBC_URL = "jdbc:h2:./converterbot";
    private static final String JDBC_USER = "converter";
    private static final String JDBC_PASSWORD = "";

    /** Создает таблицы, если они ещё не существует */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    public void initSchema() throws  SQLException {
        try (
                var connection = getConnection();
                var statement = connection.createStatement()
        ) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS custom_conversions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    chat_id BIGINT NOT NULL,
                    group_code VARCHAR(64) NOT_NULL,
                    from_unit VARCHAR(64) NOT NULL,
                    to_unit VARCHAR(64) NOT NULL,
                    rate DECIMAL(20, 10) NOT_NULL
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_custom_conv UNIQUE (chat_id, from_unit, to_unit)
                    )
                    """);
        }
    }
}
