package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.converters.QuantityConverter;
import ru.urfu.converterbot.service.models.QuantityType;

import java.util.Optional;

/**
 * Каталог конвертеров: находит конвертер по единице измерения или по команде справки.
 */
public interface ConverterProvider {
    /**
     * Ищет конвертер, которому принадлежит указанная единица измерения.
     *
     * @param unit единица измерения из запроса пользователя
     * @return конвертер группы или {@link Optional#empty()}, если группы нет
     */
    Optional<QuantityConverter> findConverterByUnitName(QuantityType unit);

    /**
     * Ищет конвертер по команде справки. Регистр команды не важен.
     *
     * @param command команда справки, например {@code /currency}
     * @return конвертер группы или {@link Optional#empty()}, если команда неизвестна
     */
    Optional<QuantityConverter> findConverterByCommand(String command);

}
