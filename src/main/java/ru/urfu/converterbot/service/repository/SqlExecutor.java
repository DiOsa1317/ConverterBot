package ru.urfu.converterbot.service.repository;

import ru.urfu.converterbot.db.DatabaseConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Утилита для выполнения SQL-запросов с try-with-resources.
 */
public final class SqlExecutor {

    private SqlExecutor() {
    }

    /**
     * Функциональный интерфейс для действия над подготовленным оператором.
     * Позволяет пробрасывать {@link SQLException} из лямбды.
     *
     * @param <T> тип результата
     */
    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(PreparedStatement statement) throws SQLException;
    }

    /**
     * Выполняет запрос, создавая соединение и подготовленный оператор.
     *
     * @param dbConfig связь с базой данных
     * @param sql      текст запроса
     * @param action   действие над подготовленным оператором
     * @param <T>      тип результата
     * @return результат действия
     * @throws SQLException при ошибке доступа к БД
     */
    public static <T> T execute(DatabaseConfig dbConfig, String sql, SqlFunction<T> action)
            throws SQLException {
        try (var connection = dbConfig.getConnection();
             var statement = connection.prepareStatement(sql)) {
            return action.apply(statement);
        }
    }
}
