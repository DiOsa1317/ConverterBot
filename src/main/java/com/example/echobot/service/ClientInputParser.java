package com.example.echobot.service;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.PhysicalQuantities;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.QuantityType;


/**
 * Парсер входных сообщений клиента для извлечения единиц измерения.
 * Разбирает сообщения формата "[число] [единица1] to [единица2]"
 * и преобразует единицы в значения enum.
 *
 */
public class ClientInputParser {

    /**
     * Разбирает сообщение клиента и извлекает две единицы измерения.
     *
     * @param message   входное сообщение, например "5 meters to feet"
     * @param enumClass класс enum с возможными единицами измерения
     * @return пара: ключ — первая единица, значение — вторая единица
     * @throws IllegalArgumentException если формат сообщения неверный
     *                                  или единицы не найдены в enum
     * @throws NullPointerException     если параметры null
     */
    public QuantityModel parse(String message) {
        var parts = message.split(" ");

        if (parts.length != 4 || !parts[2].equalsIgnoreCase("to")) {
            throw new IllegalArgumentException("Сообщение должно выглядеть как «[число] [единица 1] to [единица 2]»");
        }

        var value = Double.parseDouble(parts[0]);
        var fromStr = parts[1].toUpperCase();
        var toStr = parts[3].toUpperCase();

        QuantityType from = parseUnit(fromStr);
        QuantityType to = parseUnit(toStr);

        return new QuantityModel(value, from, to);
    }

    /**
     * Распознаёт строковое имя единицы измерения, пробуя оба известных enum-типа.
     *
     * @param name строковое имя единицы измерения
     * @return распознанное значение
     * @throws IllegalArgumentException если имя не найдено ни в одном из известных enum
     */
    private QuantityType parseUnit(String name) {
        try {
            return CurrencyType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            return PhysicalQuantities.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }

        throw new IllegalArgumentException("Неизвестная единица измерения: " + name);
    }
}

