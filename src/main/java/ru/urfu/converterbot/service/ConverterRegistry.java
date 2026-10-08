package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.QuantityType;

import java.util.List;
import java.util.Optional;

/**
 * Реестр конвертеров величин. Хранит список зарегистрированных конвертеров
 * и находит нужный по единице измерения или по команде справки.
 * Благодаря реестру диспетчер не знает про конкретные группы величин:
 * добавление новой группы сводится к регистрации ещё одного конвертера.
 */
public class ConverterRegistry {

    /** Копия списка, переданного при создании, в неизменяемом виде. */
    private final List<QuantityConverter> converters;

    /**
     * Создаёт реестр и защищает переданный список от внешних изменений.
     *
     * @param converters конвертеры, которые должен обслуживать реестр
     */
    public ConverterRegistry(List<QuantityConverter> converters) {
        this.converters = List.copyOf(converters);
    }

    /**
     * Возвращает все зарегистрированные конвертеры в порядке регистрации.
     *
     * @return неизменяемый список конвертеров
     */
    public List<QuantityConverter> all() {
        return converters;
    }

    /**
     * Ищет конвертер, которому принадлежит указанная единица измерения.
     *
     * @param unit единица измерения из запроса пользователя
     * @return конвертер группы или {@link Optional#empty()}, если такой группы нет
     */
    public Optional<QuantityConverter> findConverterByUnitName(QuantityType unit) {
        return converters.stream()
                .filter(converter -> converter.quantityType().isInstance(unit))
                .findFirst();
    }

    /**
     * Ищет конвертер по команде справки. Регистр команды не важен.
     *
     * @param command команда справки, например {@code /currency}
     * @return конвертер группы или {@link Optional#empty()}, если команда неизвестна
     */
    public Optional<QuantityConverter> findConverterByCommand(String command) {
        return converters.stream()
                .filter(converter -> converter.command().equalsIgnoreCase(command))
                .findFirst();
    }
}
