package ru.urfu.converterbot.service.models;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для типов величины для перевода
 */
public interface QuantityType {

    /**
     * Возвращает канонический код единицы — первый из списка {@link #getCodes()}.
     * Используется для отображения единицы в ответе пользователю.
     *
     * @return основной код единицы
     */
    default String getCode() {
        return getCodes().getFirst();
    }

    /**
     * Возвращает все допустимые строки ввода
     *
     * @return неизменяемый список кодов и синонимов
     */
    List<String> getCodes();

    /**
     * Проверяет, соответствует ли строка пользователя этой единице, сравнивая
     * её без учёта регистра со всеми кодами из {@link #getCodes()}.
     *
     * @param token строка, введённая пользователем
     * @return {@code true}, если строка совпала с кодом или синонимом
     */
    default boolean matches(String token) {
        return getCodes().stream()
                .anyMatch(code -> code.equalsIgnoreCase(token));
    }

    /**
     * Ищет единицу в переданном enum по её коду или синониму.
     * Статический, так как перебирает все константы, а не одну.
     *
     * @param type  класс enum с единицами измерения
     * @param token строка, введённая пользователем
     * @param <T>   тип перечисления, реализующий {@link QuantityType}
     * @return найденная единица или пустой {@link Optional}, если код не распознан
     */
    static <T extends Enum<T> & QuantityType> Optional<T> fromCode(Class<T> type, String token) {
        return Arrays.stream(type.getEnumConstants())
                .filter(unit -> unit.matches(token))
                .findFirst();
    }
}
