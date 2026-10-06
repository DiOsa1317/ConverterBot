package com.example.echobot.service;

import com.example.echobot.service.models.QuantityType;

import java.util.List;
import java.util.Optional;

public class ConverterRegistry {

    private final List<QuantityConverter> converters;

    public ConverterRegistry(List<QuantityConverter> converters) {
        this.converters = List.copyOf(converters);
    }

    /** Все зарегистрированные конвертеры в порядке регистрации. */
    public List<QuantityConverter> all() {
        return converters;
    }

    /** Ищет подходящий конвертер для указанного типа */
    public Optional<QuantityConverter> findConverterByUnitName(QuantityType unit) {
        return converters.stream()
                .filter(converter -> converter.quantityType().isInstance(unit))
                .findFirst();
    }

    /** Ищет подходящий конвертер для указанной команды */
    public Optional<QuantityConverter> findConverterByCommand(String command) {
        return converters.stream()
                .filter(converter -> converter.command().equalsIgnoreCase(command))
                .findFirst();
    }
}
