package com.example.echobot.service;


import java.util.AbstractMap;
import java.util.Map;


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
    public <T extends Enum<T>> Map.Entry<T, T> parse(String message, Class<T> enumClass) {
        String[] parts = message.split(" ");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Сообщение должно выглядеть как «[число] [единица 1] to [единица 2]»");
        }

        T first = Enum.valueOf(enumClass, parts[0].toUpperCase());
        T second = Enum.valueOf(enumClass, parts[1].toUpperCase());

        return new AbstractMap.SimpleImmutableEntry<>(first, second);
    }

}

