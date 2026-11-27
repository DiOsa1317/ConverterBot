package ru.urfu.converterbot.service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.urfu.converterbot.db.DatabaseConfig;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**Класс, отвечающий за доступ к данным о кастомных переводах*/
public class CustomConversionRepositoryImp implements CustomConversionRepository {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomConversionRepositoryImp.class);

    private final DatabaseConfig dbConfig;

    /** Конструктор
     * @param dbConfig - связь с базой данных
     * */
    public CustomConversionRepositoryImp(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override 
    public boolean save(CustomConversion conversion) {
        String insertSql = """
                INSERT INTO custom_conversions (chat_id, from_unit, to_unit, rate, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        String checkSql = """
                SELECT 1 FROM custom_conversions
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try {
            boolean exists = SqlExecutor.execute(dbConfig, checkSql, statement -> {
                bindCheck(statement, conversion);
                return statement.executeQuery().next();
            });

            if (exists) {
                throw new SQLException("Такой перевод уже существует");
            }

            return SqlExecutor.execute(dbConfig, insertSql, statement -> {
                bindSave(statement, conversion);
                return statement.executeUpdate() > 0;
            });

        } catch (SQLException exception) {
            logger.error("Не удалось сохранить кастомный перевод: {}", conversion, exception);
            return false;
        }
    }

    @Override 
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

        try {
            return SqlExecutor.execute(dbConfig, sql, statement -> {
                bindFind(statement, chatId, from, to);

                var resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapConversion(resultSet, chatId));
            });

        } catch (SQLException exception) {
            logger.error(
                    "Не удалось найти кастомный перевод: chatId={}, from={}, to={}",
                    chatId, from, to, exception
            );
            return Optional.empty();
        }
    }

    @Override 
    public List<CustomConversion> findAllOutgoing(long chatId) {
        var sql = """
                SELECT from_unit, to_unit, rate
                FROM custom_conversions
                WHERE chat_id = ?
                ORDER BY from_unit, to_unit
                """;

        try {
            return SqlExecutor.execute(dbConfig, sql, statement -> {
                statement.setLong(1, chatId);

                var resultSet = statement.executeQuery();
                List<CustomConversion> conversions = new ArrayList<>();
                while (resultSet.next()) {
                    conversions.add(mapConversion(resultSet, chatId));
                }
                return conversions;
            });

        } catch (SQLException exception) {
            logger.error("Не удалось получить кастомные переводы для chatId={}",
                    chatId, exception);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) {
        var sql = """
                DELETE FROM custom_conversions
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try {
            return SqlExecutor.execute(dbConfig, sql, statement -> {
                bindDelete(statement, chatId, from, to);
                return statement.executeUpdate() > 0;
            });

        } catch (SQLException exception) {
            logger.error(
                    "Не удалось удалить кастомный перевод: chatId={}, from={}, to={}",
                    chatId, from, to, exception
            );
            return false;
        }
    }

    @Override
    public boolean updateCustomConversion(long chatId, String from, String to, BigDecimal rate) {
        String updateSql = """
                UPDATE custom_conversions
                SET rate = ?, created_at = CURRENT_TIMESTAMP
                WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
                """;

        try {
            return SqlExecutor.execute(dbConfig, updateSql, statement -> {
                bindUpdate(statement, chatId, from, to, rate);
                return statement.executeUpdate() > 0;
            });

        } catch (SQLException exception) {
            logger.error(
                    "Не удалось обновить кастомный перевод: chatId={}, from={}, to={}",
                    chatId, from, to, exception
            );
            return false;
        }
    }

    @Override 
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

        try {
            return SqlExecutor.execute(dbConfig, sql, statement -> {
                statement.setLong(1, chatId);
                statement.setLong(2, chatId);

                var resultSet = statement.executeQuery();
                return readUnitNames(resultSet);
            });

        } catch (SQLException exception) {
            logger.error("Не удалось получить названия кастомных единиц для chatId={}",
                    chatId, exception);
            return new ArrayList<>();
        }
    }

    /**
     * Заполняет параметры проверки существования конверсии.
     */
    private void bindCheck(PreparedStatement statement, CustomConversion conversion) throws SQLException {
        statement.setLong(1, conversion.chatId());
        statement.setString(2, conversion.fromUnit());
        statement.setString(3, conversion.toUnit());
    }

    /**
     * Заполняет параметры вставки конверсии.
     */
    private void bindSave(PreparedStatement statement, CustomConversion conversion) throws SQLException {
        statement.setLong(1, conversion.chatId());
        statement.setString(2, conversion.fromUnit());
        statement.setString(3, conversion.toUnit());
        statement.setBigDecimal(4, conversion.rate());
    }

    /**
     * Заполняет параметры поиска конверсии.
     */
    private void bindFind(PreparedStatement statement, long chatId, String from, String to) throws SQLException {
        statement.setLong(1, chatId);
        statement.setString(2, from);
        statement.setString(3, to);
    }

    /**
     * Заполняет параметры удаления конверсии.
     */
    private void bindDelete(PreparedStatement statement, long chatId, String from, String to) throws SQLException {
        statement.setLong(1, chatId);
        statement.setString(2, from);
        statement.setString(3, to);
    }

    /**
     * Заполняет параметры обновления курса конверсии.
     */
    private void bindUpdate(PreparedStatement statement, long chatId, String from, String to, BigDecimal rate)
            throws SQLException {
        statement.setBigDecimal(1, rate);
        statement.setLong(2, chatId);
        statement.setString(3, from);
        statement.setString(4, to);
    }

    /**
     * Преобразует строку результата в {@link CustomConversion}.
     */
    private CustomConversion mapConversion(ResultSet resultSet, long chatId) throws SQLException {
        return new CustomConversion(
                resultSet.getString("from_unit"),
                resultSet.getString("to_unit"),
                resultSet.getBigDecimal("rate"),
                chatId
        );
    }

    /**
     * Читает названия единиц из результата запроса.
     */
    private List<String> readUnitNames(ResultSet resultSet) throws SQLException {
        List<String> names = new ArrayList<>();
        while (resultSet.next()) {
            names.add(resultSet.getString("unit"));
        }
        return names;
    }
}
