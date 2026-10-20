package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.*;

import java.math.BigDecimal;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Парсер входных сообщений клиента для извлечения единиц измерения.
 */
public class ClientInputParser implements Parser {

    /** Слово-разделитель между единицами измерения. */
    private static final String TO_SEPARATOR = "to";

    @Override 
    public ConversionRequest parse(@NotNull String message) throws InvalidUserInputException {
        Objects.requireNonNull(message, "Сообщение не может быть null");
        var parts = message.split(" ");

        if (parts.length != 4 || !parts[2].equalsIgnoreCase(TO_SEPARATOR)) {
            throw new InvalidUserInputException(message,
                    "Сообщение должно выглядеть как «[число] [единица 1] to [единица 2]»");
        }

        var value = parseValue(message, parts[0]);
        var from = parseUnit(message, parts[1]);
        var to = parseUnit(message, parts[3]);

        return new ConversionRequest(value, from, to);
    }

    /**
     * Разбирает первый токен сообщения как число.
     *
     * @param originalMessage исходное сообщение целиком (для текста ошибки)
     * @param token текстовый токен с числовым значением
     * @return разобранное значение
     * @throws InvalidUserInputException если токен не является числом
     */
    private BigDecimal parseValue(String originalMessage, String token) throws InvalidUserInputException {
        try {
            return new BigDecimal(token);
        } catch (NumberFormatException exception) {
            throw new InvalidUserInputException(originalMessage,
                    "Первое слово должно быть числом. Например: 100 USD to EUR");
        }
    }

    /**
     * Распознаёт строковое имя единицы измерения, перебирая все известные
     * enum-типы величин: валюты, длину, массу и температуру.
     *
     * @param originalMessage исходное сообщение целиком (для текста ошибки)
     * @param token строковое имя единицы измерения
     * @return распознанное значение
     * @throws InvalidUserInputException если имя не найдено ни в одном из известных enum
     */
    private QuantityType parseUnit(String originalMessage, String token) throws InvalidUserInputException {
        return QuantityType.fromCode(CurrencyType.class, token)
                .<QuantityType>map(unit -> unit)
                .or(() -> QuantityType.fromCode(LengthType.class, token).map(unit -> (QuantityType) unit))
                .or(() -> QuantityType.fromCode(WeightType.class, token).map(unit -> (QuantityType) unit))
                .or(() -> QuantityType.fromCode(TemperatureType.class, token).map(unit -> (QuantityType) unit))
                .orElseThrow(() -> new InvalidUserInputException(
                        originalMessage, "Неизвестная единица измерения: " + token));
    }
}

