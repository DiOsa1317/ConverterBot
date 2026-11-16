package ru.urfu.converterbot.service;

import ru.urfu.converterbot.keyboard.service.DatabaseConfig;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**Класс, отвечающий за доступ к данным о кастомных переводах*/
public class CustomConversionRepository {
    private final DatabaseConfig dbConfig;

    /** Конструктор
     * @param dbConfig - связь с базой данных
     * */
    public CustomConversionRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /** Метод сохранения данных о новом кастомном переводе или обновление данных о существующем
     * @param conversion данные о кастомном переводе
     * @throws SQLException ошибка при взаимодействии с базой данных
     * */
    public void saveOrUpdate(CustomConversion conversion) throws SQLException {
        String checkSql = "SELECT 1 FROM custom_conversions WHERE chat_id = ? AND from_unit = ? AND to_unit = ?";
        String insertSql = """
            INSERT INTO custom_conversions (chat_id, from_unit, to_unit, rate, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        String updateSql = """
            UPDATE custom_conversions SET rate = ?, created_at = CURRENT_TIMESTAMP
            WHERE chat_id = ? AND from_unit = ? AND to_unit = ?
            """;
        try (var connection = dbConfig.getConnection()) {
            try (var ps = connection.prepareStatement(checkSql)) {
                ps.setLong(1, conversion.chatId());
                ps.setString(2, conversion.fromUnit());
                ps.setString(3, conversion.toUnit());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (var ups = connection.prepareStatement(updateSql)) {
                            ups.setBigDecimal(1, conversion.rate());
                            ups.setLong(2, conversion.chatId());
                            ups.setString(3, conversion.fromUnit());
                            ups.setString(4, conversion.toUnit());
                            ups.executeUpdate();
                        }
                        return;
                    }
                }
            }
            try (var ps = connection.prepareStatement(insertSql)) {
                ps.setLong(1, conversion.chatId());
                ps.setString(2, conversion.fromUnit());
                ps.setString(3, conversion.toUnit());
                ps.setBigDecimal(4, conversion.rate());
                ps.executeUpdate();
            }
        }
    }

    /** Метод поиска кастомного перевода пользователя
     * @param chatId id чата с пользователем
     * @param from название кастомной единицы (из чего переводим)
     * @param to название кастомной единицы (куда переводим)
     * @throws SQLException ошибка при взаимодействии с базой данных
     * */
    public Optional<CustomConversion> findByChatIdFromAndAndTo(long chatId, String from, String to)
    throws SQLException {
        var sql = "SELECT from_unit, to_unit, rate FROM custom_conversions WHERE chat_id = ? AND from_unit = ? AND to_unit = ?";
        try(
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
                ) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, from);
            preparedStatement.setString(3, to);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new CustomConversion(
                            resultSet.getString("from_unit"),
                            resultSet.getString("to_unit"),
                            resultSet.getBigDecimal("rate"),
                            chatId
                    ));
                }
                return Optional.empty();
            }
        }
    }

    /** Метод поиска всех кастомных переводов пользователя
     * @param chatId id чата с пользователем
     * @throws SQLException ошибка при взаимодействии с базой данных
     * */
    public List<CustomConversion> findAllOutgoing(long chatId) throws SQLException {
        var sql = "SELECT from_unit, to_unit, rate FROM custom_conversions WHERE chat_id = ? ORDER BY from_unit, to_unit";
        List<CustomConversion> conversionsByIdList = new ArrayList<>();
        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
                ) {
            preparedStatement.setLong(1, chatId);
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    conversionsByIdList.add(new CustomConversion(
                            resultSet.getString("from_unit"),
                            resultSet.getString("to_unit"),
                            resultSet.getBigDecimal("rate"),
                            chatId
                    ));
                }
            }
        }
        return conversionsByIdList;
    }

    /** Метод удаления кастомного перевода пользователя
     * @param chatId id чата с пользователем
     * @param from название кастомной единицы (из чего переводим)
     * @param to название кастомной единицы (куда переводим)
     * @throws SQLException ошибка при взаимодействии с базой данных
     * */
    public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) throws SQLException {
        var sql = "DELETE FROM custom_conversions WHERE chat_id = ? AND from_unit = ? AND to_unit = ?";
        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, from);
            preparedStatement.setString(3, to);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    /** Метод поиска всех названий кастомных переводов пользователя
     * @param chatId id чата с пользователем
     * @throws SQLException ошибка при взаимодействии с базой данных
     * */
    public List<String> findAllUnitNames(long chatId) throws SQLException {
        var sql = """
                SELECT DISTINCT from_unit AS unit FROM custom_conversions WHERE chat_id = ?
                UNION
                SELECT DISTINCT to_unit FROM custom_conversions WHERE chat_id = ?
                """;
        List<String> names = new ArrayList<>();
        try (var connection = dbConfig.getConnection();
             var ps = connection.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, chatId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("unit"));
                }
            }
        }
        return names;
    }

}
