package com.example.echobot.service;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.LengthType;
import com.example.echobot.service.models.TemperatureType;
import com.example.echobot.service.models.WeightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        registry = new ConverterRegistry(List.of(
               new CurrencyConverter(),
               new LengthConverter(),
               new WeightConverter(),
               new TemperatureConverter()
        ));
    }

    /**
     * Проверяет, что по единице измерения находится конвертер её группы.
     */
    @Test
    @DisplayName("Находит конвертер по единице измерения")
    void shouldFindConverterByUnit() {
        assertEquals(CurrencyType.class,
                registry.findConverterByUnitName(CurrencyType.USD).orElseThrow().quantityType());
        assertEquals(LengthType.class,
                registry.findConverterByUnitName(LengthType.KILOMETER).orElseThrow().quantityType());
        assertEquals(TemperatureType.class,
                registry.findConverterByUnitName(TemperatureType.KELVIN).orElseThrow().quantityType());
        assertEquals(WeightType.class,
                registry.findConverterByUnitName(WeightType.POUND).orElseThrow().quantityType());
    }

    /**
     * Проверяет, что поиск команды справки не зависит от регистра.
     */
    @Test
    @DisplayName("Находит конвертер по команде справки в любом регистре")
    void shouldFindConverterByCommand() {
        assertEquals("Валюты",
                registry.findConverterByCommand("/currency").orElseThrow().title());
        assertEquals("Температура",
                registry.findConverterByCommand("/TEMPERATURE").orElseThrow().title());
    }

    /**
     * Проверяет, что неизвестная команда не находит конвертер.
     */
    @Test
    @DisplayName("Возвращает пустой результат для неизвестной команды")
    void shouldReturnEmptyForUnknownCommand() {
        assertTrue(registry.findConverterByCommand("/physical").isEmpty());
    }

    /**
     * Проверяет, что порядок конвертеров в реестре совпадает
     * с порядком их регистрации.
     */
    @Test
    @DisplayName("Сохраняет порядок регистрации конвертеров")
    void shouldKeepRegistrationOrder() {
        assertEquals(List.of("Валюты", "Длина", "Вес", "Температура"),
                registry.all().stream().map(QuantityConverter::title).toList());
    }
}
