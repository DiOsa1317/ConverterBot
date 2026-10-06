package com.example.echobot.service;

import com.example.echobot.service.models.*;


/**
 * Парсер входных сообщений клиента для извлечения единиц измерения.
 * Разбирает сообщения формата "[число] [единица1] to [единица2]"
 * и преобразует единицы в значения enum.
 *
 */
public class ClientInputParser {

    /** Слово-разделитель между единицами измерения. */
    private static  final  String TO_SEPARATOR = "to";

    /**
     * Разбирает сообщение клиента в запрос на перевод.
     *
     * @param message   входное сообщение, например "100 USD to EUR"
     * @return разобранный запрос: значение, исходная единица и целевая единица
     * @throws ConversionException если формат сообщения неверный
     *                                  или единица неизвестна
     * @throws NullPointerException если передан {@code null}
     */
    public QuantityModel parse(String message) {
        var parts = message.split(" ");

        if (parts.length != 4 || !parts[2].equalsIgnoreCase(TO_SEPARATOR)) {
            throw new ConversionException("Сообщение должно выглядеть как «[число] [единица 1] to [единица 2]»");
        }

        var value = parseValue(parts[0]);
        var from = parseUnit(parts[1]);
        var to = parseUnit(parts[3]);

        return new QuantityModel(value, from, to);
    }

    /**
     * Разбирает первый токен сообщения как число.
     *
     * @param token текстовый токен с числовым значением
     * @return разобранное значение
     * @throws ConversionException если токен не является числом
     */
    private double parseValue(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException exception) {
            throw new ConversionException("Первое слово должно быть числом. Например: 100 USD to EUR");
        }
    }

    /**
     * Распознаёт строковое имя единицы измерения, перебирая все известные
     * enum-типы величин: валюты, длину, массу и температуру.
     *
     * @param token строковое имя единицы измерения
     * @return распознанное значение
     * @throws ConversionException если имя не найдено ни в одном из известных enum
     */
    private QuantityType parseUnit(String token) {
        var unit = token.toUpperCase();
        try {
            return CurrencyType.valueOf(unit);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            return LengthType.valueOf(unit);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            return WeightType.valueOf(unit);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            return TemperatureType.valueOf(unit);
        } catch (IllegalArgumentException ignored) {
        }

        throw new ConversionException("Неизвестная единица измерения: " + token);
    }
}

