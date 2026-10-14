package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.LengthType;
import ru.urfu.converterbot.service.models.TemperatureType;
import ru.urfu.converterbot.service.models.WeightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты для ConverterRegistry */
public class ConverterRegistryTest {

    /** Реестр конвертеров, создаваемый заново перед каждым тестом. */
    private ConverterRegistry registry;

    /**
     * Создаёт реестр со всеми четырьмя конвертерами перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        registry = new ConverterRegistry();
    }

    /**
     * Проверяет, что по единице измерения находится конвертер её группы.
     */
    @Test
    @DisplayName("Находит конвертер по единице измерения")
    void shouldFindConverterByUnit() {
        assertEquals(CurrencyConverter.class,
                registry.findConverterByUnitName(CurrencyType.USD).orElseThrow().getClass());
        assertEquals(LengthConverter.class,
                registry.findConverterByUnitName(LengthType.KILOMETER).orElseThrow().getClass());
        assertEquals(TemperatureConverter.class,
                registry.findConverterByUnitName(TemperatureType.KELVIN).orElseThrow().getClass());
        assertEquals(WeightConverter.class,
                registry.findConverterByUnitName(WeightType.POUND).orElseThrow().getClass());
    }

    /**
     * Проверяет, что неизвестная команда не находит конвертер.
     */
    @Test
    @DisplayName("Возвращает пустой результат для неизвестной команды")
    void shouldReturnEmptyForUnknownCommand() {
        assertTrue(registry.findConverterByCommand("/physical").isEmpty());
    }

}
