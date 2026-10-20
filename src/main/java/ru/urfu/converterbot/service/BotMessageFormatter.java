package ru.urfu.converterbot.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

/**
 * Формирует тексты сообщений, которые бот показывает пользователю.
 */
public class BotMessageFormatter implements MessageFormatter{

    private static final String RESULT_TEMPLATE = "%.4f %s = %.4f %s";

    private static final String RATE_LINE_TEMPLATE = "1 %s = %.4f %s%n";

    private static final String HELP_TEXT = """
            Формат запроса: {число} {единица1} to {единица2}
            Например: 100 USD to EUR
            Или: 10 KM to MI

            Доступные валюты: /currency
            Доступные величины:
            1) /length
            2) /weight
            3) /temperature
            """;

    private static final String START_TEXT = """
            Приветствую вас! Этот бот конвертирует валюты и физические величины.
            Чтобы узнать подробнее, введите /help
            """;

    private static final String NO_TEXT_MESSAGE_TEXT =
            "Я понимаю только текстовые сообщения. Введите /help, чтобы узнать формат запроса.";

    private static final String INVALID_INPUT_TEXT =
            "Не удалось разобрать запрос. Формат: {число} {единица1} to {единица2}. Подробнее: /help";

    private static final String UNEXPECTED_ERROR_TEXT =
            "Произошла непредвиденная ошибка. Попробуйте ещё раз.";

    @Override 
    public String start() {
        return START_TEXT;
    }

    @Override 
    public String help() {
        return HELP_TEXT;
    }

    @Override 
    public String noText() {
        return NO_TEXT_MESSAGE_TEXT;
    }

    @Override 
    public String invalidInput() {
        return INVALID_INPUT_TEXT;
    }

    @Override 
    public String unexpectedError() {
        return UNEXPECTED_ERROR_TEXT;
    }

    @Override
    public String rates(String title, QuantityType baseUnit, Map<QuantityType, BigDecimal> rates) {
        var builder = new StringBuilder(title).append(":").append(System.lineSeparator());
        rates.forEach((unit, rate) -> builder.append(String.format(Locale.US, RATE_LINE_TEMPLATE,
                baseUnit.getCode(), rate, unit.getCode())));
        return builder.toString();
    }

    @Override 
    public String getResult(ConversionRequest request, BigDecimal result) {
        return String.format(Locale.US, RESULT_TEMPLATE,
                request.value(), request.from().getCode(),
                result, request.to().getCode());
    }
}