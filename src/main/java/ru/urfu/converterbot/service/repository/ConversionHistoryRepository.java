package ru.urfu.converterbot.service.repository;

import ru.urfu.converterbot.keyboard.service.DatabaseConfig;
import ru.urfu.converterbot.service.models.ConversionHistory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, отвечающий за доступ к истории конвертаций.
 */
public class ConversionHistoryRepository {

    private final DatabaseConfig dbConfig;

    public ConversionHistoryRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Сохраняет новую запись в истории конвертаций.
     *
     * @param chatId ID чата с пользователем
     * @param input исходный запрос пользователя
     * @param result результат конвертации
     * @throws SQLException ошибка при взаимодействии с БД
     */
    public void save(long chatId, String input, String result) throws SQLException {
        var sql = """
                INSERT INTO conversion_history (chat_id, input, result, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, input);
            preparedStatement.setString(3, result);

            preparedStatement.executeUpdate();
        }
    }

    /**
     * Возвращает страницу истории пользователя.
     *
     * @param chatId ID чата с пользователем
     * @param limit количество записей на странице
     * @param offset количество записей, которые нужно пропустить
     * @return список записей истории
     * @throws SQLException ошибка при взаимодействии с БД
     */
    public List<ConversionHistory> findPage(
            long chatId,
            int limit,
            int offset
    ) throws SQLException {

        var sql = """
                SELECT id, chat_id, input, result, created_at
                FROM conversion_history
                WHERE chat_id = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        List<ConversionHistory> history = new ArrayList<>();

        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, offset);

            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("created_at");

                    history.add(new ConversionHistory(
                            resultSet.getLong("id"),
                            resultSet.getLong("chat_id"),
                            resultSet.getString("input"),
                            resultSet.getString("result"),
                            timestamp.toLocalDateTime()
                    ));
                }
            }
        }

        return history;
    }

    /**
     * Возвращает количество записей истории пользователя.
     *
     * @param chatId ID чата с пользователем
     * @return количество записей
     * @throws SQLException ошибка при взаимодействии с БД
     */
    public int count(long chatId) throws SQLException {
        var sql = """
                SELECT COUNT(*)
                FROM conversion_history
                WHERE chat_id = ?
                """;

        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, chatId);

            try (var resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * Удаляет всю историю конвертаций пользователя.
     *
     * @param chatId ID чата с пользователем
     * @return true, если была удалена хотя бы одна запись
     * @throws SQLException ошибка при взаимодействии с БД
     */
    public boolean deleteAll(long chatId) throws SQLException {
        var sql = """
                DELETE FROM conversion_history
                WHERE chat_id = ?
                """;

        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, chatId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}