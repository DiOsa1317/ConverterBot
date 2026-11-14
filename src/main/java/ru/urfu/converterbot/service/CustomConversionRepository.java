package ru.urfu.converterbot.service;

import ru.urfu.converterbot.keyboard.service.DatabaseConfig;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomConversionRepository {
    private final DatabaseConfig dbConfig;

    public CustomConversionRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void save(CustomConversion conversion) throws SQLException {
        var sql = """
                MERGE INTO custom_conversions (chat_id, from_unit, to_unit, rate, created_at)
                KEY (chat_id, from_unit, to_unit)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, conversion.chatId());
            preparedStatement.setString(2, conversion.fromUnit());
            preparedStatement.setString(3, conversion.toUnit());
            preparedStatement.setBigDecimal(4, conversion.rate());
            preparedStatement.executeUpdate();
        }
    }

    public Optional<CustomConversion> findByChatidFromAndAndTo(long chatId, String from, String to)
    throws SQLException {
        var sql = "SELECT from_unit, to_unit, rate FROM custom_conversions WHERE chat_id = ? AND from_unit = ? AND to_unit = ?";
        try(
                var connection = dbConfig.getConnection();
                var preparedStatement = connection.prepareStatement(sql);
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

    public boolean existsByChatIdAndFromAndTo(long chatId, String from, String to) throws SQLException {
        return findByChatidFromAndAndTo(chatId, from, to).isPresent();
    }

}
