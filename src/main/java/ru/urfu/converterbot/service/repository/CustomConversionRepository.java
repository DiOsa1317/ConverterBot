package ru.urfu.converterbot.service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.urfu.converterbot.db.DatabaseConfig;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**Класс, отвечающий за доступ к данным о кастомных переводах*/
public class CustomConversionRepository {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomConversionRepository.class);

    private final DatabaseConfig dbConfig;

    /** Конструктор
     * @param dbConfig - связь с базой данных
     * */
    public CustomConversionRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Сохраняет новый кастомный перевод или обновляет существующий.
     *
     * @param conversion данные о кастомном переводе
     * @return true, если операция успешно выполнена
     */
    public boolean saveOrUpdate(CustomConversion conversion) {
        String checkSql = """
                SELECT 1 FROM custom_conversions
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        String insertSql = """
                INSERT INTO custom_conversions (chat_id, from_unit, to_unit, rate, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        String updateSql = """
                UPDATE custom_conversions
                SET rate = ?, created_at = CURRENT_TIMESTAMP
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try (var connection = dbConfig.getConnection()) {
            if (exists(connection, checkSql, conversion)) {
                return update(connection, updateSql, conversion);
            }

            return insert(connection, insertSql, conversion);

        } catch (SQLException exception) {
            logger.error("Не удалось сохранить кастомный перевод: {}", conversion, exception);
            return false;
        }
    }

    /**
     * Проверяет существование кастомного перевода.
     */
    private boolean exists(
            java.sql.Connection connection,
            String sql,
            CustomConversion conversion
    ) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, conversion.chatId());
            statement.setString(2, conversion.fromUnit());
            statement.setString(3, conversion.toUnit());

            var resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    /**
     * Обновляет существующий кастомный перевод.
     */
    private boolean update(
            java.sql.Connection connection,
            String sql,
            CustomConversion conversion
    ) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, conversion.rate());
            statement.setLong(2, conversion.chatId());
            statement.setString(3, conversion.fromUnit());
            statement.setString(4, conversion.toUnit());

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Добавляет новый кастомный перевод.
     */
    private boolean insert(
            java.sql.Connection connection,
            String sql,
            CustomConversion conversion
    ) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, conversion.chatId());
            statement.setString(2, conversion.fromUnit());
            statement.setString(3, conversion.toUnit());
            statement.setBigDecimal(4, conversion.rate());

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Ищет кастомный перевод пользователя.
     *
     * @param chatId ID чата
     * @param from исходная единица
     * @param to целевая единица
     * @return найденный перевод или Optional.empty() при отсутствии или ошибке
     */
    public Optional<CustomConversion> findByChatIdFromAndAndTo(
            long chatId,
            String from,
            String to
    ) {
        var sql = """
                SELECT from_unit, to_unit, rate
                FROM custom_conversions
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try (
                var connection = dbConfig.getConnection();
                var statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, chatId);
            statement.setString(2, from);
            statement.setString(3, to);

            var resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(new CustomConversion(
                    resultSet.getString("from_unit"),
                    resultSet.getString("to_unit"),
                    resultSet.getBigDecimal("rate"),
                    chatId
            ));

        } catch (SQLException exception) {
            logger.error(
                    "Не удалось найти кастомный перевод: chatId={}, from={}, to={}",
                    chatId, from, to, exception
            );
            return Optional.empty();
        }
    }

    /**
     * Возвращает все кастомные переводы пользователя.
     *
     * @param chatId ID чата
     * @return список кастомных переводов или пустой список при ошибке
     */
    public List<CustomConversion> findAllOutgoing(long chatId) {
        var sql = """
                SELECT from_unit, to_unit, rate
                FROM custom_conversions
                WHERE chat_id = ?
                ORDER BY from_unit, to_unit
                """;

        List<CustomConversion> conversions = new ArrayList<>();

        try (
                var connection = dbConfig.getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = getResultSet(statement, chatId)
        ) {
            while (resultSet.next()) {
                conversions.add(new CustomConversion(
                        resultSet.getString("from_unit"),
                        resultSet.getString("to_unit"),
                        resultSet.getBigDecimal("rate"),
                        chatId
                ));
            }

        } catch (SQLException exception) {
            logger.error("Не удалось получить кастомные переводы для chatId={}",
                    chatId, exception);
            return new ArrayList<>();
        }

        return conversions;
    }

    /**
     * Выполняет запрос и возвращает ResultSet.
     */
    private java.sql.ResultSet getResultSet(
            java.sql.PreparedStatement statement,
            long chatId
    ) throws SQLException {
        statement.setLong(1, chatId);
        return statement.executeQuery();
    }

    /**
     * Удаляет кастомный перевод пользователя.
     *
     * @param chatId ID чата
     * @param from исходная единица
     * @param to целевая единица
     * @return true, если перевод удалён
     */
    public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) {
        var sql = """
                DELETE FROM custom_conversions
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try (
                var connection = dbConfig.getConnection();
                var statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, chatId);
            statement.setString(2, from);
            statement.setString(3, to);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            logger.error(
                    "Не удалось удалить кастомный перевод: chatId={}, from={}, to={}",
                    chatId, from, to, exception
            );
            return false;
        }
    }

    /**
     * Возвращает все названия кастомных единиц пользователя.
     *
     * @param chatId ID чата
     * @return список названий или пустой список при ошибке
     */
    public List<String> findAllUnitNames(long chatId) {
        var sql = """
                SELECT DISTINCT from_unit AS unit
                FROM custom_conversions
                WHERE chat_id = ?
                UNION
                SELECT DISTINCT to_unit
                FROM custom_conversions
                WHERE chat_id = ?
                """;

        List<String> names = new ArrayList<>();

        try (
                var connection = dbConfig.getConnection();
                var statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, chatId);
            statement.setLong(2, chatId);

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                names.add(resultSet.getString("unit"));
            }

        } catch (SQLException exception) {
            logger.error("Не удалось получить названия кастомных единиц для chatId={}",
                    chatId, exception);
            return new ArrayList<>();
        }

        return names;
    }
}
