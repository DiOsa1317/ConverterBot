package ru.urfu.converterbot.service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.urfu.converterbot.db.DatabaseConfig;
import ru.urfu.converterbot.service.models.ConversionHistory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, отвечающий за доступ к истории конвертаций.
 */
public class ConversionHistoryRepositoryImp implements ConversionHistoryRepository {

    private static final Logger logger =
            LoggerFactory.getLogger(ConversionHistoryRepositoryImp.class);

    private final DatabaseConfig dbConfig;

    public ConversionHistoryRepositoryImp(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override 
    public boolean save(long chatId, String input, String result) {
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

            return preparedStatement.executeUpdate() > 0;

        } catch (Exception exception) {
            logger.error("Не удалось сохранить историю конвертаций для chatId={}",
                    chatId, exception);
            return false;
        }
    }

    @Override
    public List<ConversionHistory> findPage(long chatId, int limit, int offset) {
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

            var resultSet = preparedStatement.executeQuery();

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

        } catch (Exception exception) {
            logger.error("Не удалось получить историю конвертаций для chatId={}",
                    chatId, exception);
            return new ArrayList<>();
        }

        return history;
    }

    @Override
    public int count(long chatId) {
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

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;

        } catch (Exception exception) {
            logger.error("Не удалось посчитать записи истории для chatId={}",
                    chatId, exception);
            return 0;
        }
    }

    @Override
    public boolean deleteAll(long chatId) {
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

        } catch (Exception exception) {
            logger.error("Не удалось удалить историю конвертаций для chatId={}",
                    chatId, exception);
            return false;
        }
    }
}
