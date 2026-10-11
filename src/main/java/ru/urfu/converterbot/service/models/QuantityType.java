package ru.urfu.converterbot.service.models;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для типов величины для перевода
 */
public interface QuantityType {
    default String getCode() {
        return getCodes().getFirst();
    }

    /**
     * Возвращает все допустимые строки ввода. По умолчанию — только канонический код.
     *
     * @return неизменяемый список кодов и синонимов
     */
    default List<String> getCodes() {
        return List.of(((Enum<?>) this).name());
    }

    default boolean matches(String token) {
        return getCodes().stream()
                .anyMatch(code -> code.equalsIgnoreCase(token));
    }

    static <T extends Enum<T> & QuantityType> Optional<T> fromCode(Class<T> type, String token) {
        return Arrays.stream(type.getEnumConstants())
                .filter(unit -> unit.matches(token))
                .findFirst();
    }
}
