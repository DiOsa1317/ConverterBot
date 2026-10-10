package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр конвертеров величин. Хранит список зарегистрированных конвертеров
 * и находит нужный по единице измерения или по команде справки.
 */
public class ConverterRegistry {

    private final CurrencyConverter currencyConverter = new CurrencyConverter();
    private final WeightConverter weightConverter = new WeightConverter();
    private final LengthConverter lengthConverter = new LengthConverter();
    private final TemperatureConverter temperatureConverter = new TemperatureConverter();

    /** Доступные конвертеры */
    private final Map<Class<? extends QuantityType>, QuantityConverter> convertersByTypes = new HashMap<>();

    private final Map<String, QuantityConverter> convertersByCommands = new HashMap<>();

    /**
     * Создаёт реестр и защищает переданный список от внешних изменений.
     */
    public ConverterRegistry() {
        registerConvertersByTypes();
        registerConvertersByCommands();
    }

    /**
     * Возвращает все зарегистрированные конвертеры в порядке регистрации.
     *
     * @return неизменяемый список конвертеров
     */
    public List<QuantityConverter> all() {
        return convertersByTypes.values().stream().toList();
    }

    /**
     * Ищет конвертер, которому принадлежит указанная единица измерения.
     *
     * @param unit единица измерения из запроса пользователя
     * @return конвертер группы или {@link Optional#empty()}, если такой группы нет
     */
    public Optional<QuantityConverter> findConverterByUnitName(QuantityType unit) {
        var unitClass = unit.getClass();
        if (!convertersByTypes.containsKey(unitClass))
            return Optional.empty();
        return Optional.ofNullable(convertersByTypes.get(unitClass));
    }

    /**
     * Ищет конвертер по команде справки. Регистр команды не важен.
     *
     * @param command команда справки, например {@code /currency}
     * @return конвертер группы или {@link Optional#empty()}, если команда неизвестна
     */
    public Optional<QuantityConverter> findConverterByCommand(String command) {
        var lowercaseCommand = command.toLowerCase();
        if (!convertersByCommands.containsKey(lowercaseCommand))
            return Optional.empty();
        return Optional.ofNullable(convertersByCommands.get(lowercaseCommand));
    }

    private void registerConvertersByTypes() {
        convertersByTypes.put(CurrencyType.class, currencyConverter);
        convertersByTypes.put(LengthType.class, lengthConverter);
        convertersByTypes.put(WeightType.class, weightConverter);
        convertersByTypes.put(TemperatureType.class, temperatureConverter);
    }

    private void registerConvertersByCommands() {
        convertersByCommands.put(BotCommand.CURRENCY.getCommand(), currencyConverter);
        convertersByCommands.put(BotCommand.LENGTH.getCommand(), lengthConverter);
        convertersByCommands.put(BotCommand.WEIGHT.getCommand(), weightConverter);
        convertersByCommands.put(BotCommand.TEMPERATURE.getCommand(), temperatureConverter);
    }
}
