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

    private ConverterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ConverterRegistry(List.of(
               new CurrencyConverter(),
               new LengthConverter(),
               new WeightConverter(),
               new TemperatureConverter()
        ));
    }

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

    @Test
    @DisplayName("Находит конвертер по команде справки в любом регистре")
    void shouldFindConverterByCommand() {
        assertEquals("Валюты",
                registry.findConverterByCommand("/currency").orElseThrow().title());
        assertEquals("Температура",
                registry.findConverterByCommand("/TEMPERATURE").orElseThrow().title());
    }

    @Test
    @DisplayName("Возвращает пустой результат для неизвестной команды")
    void shouldReturnEmptyForUnknownCommand() {
        assertTrue(registry.findConverterByCommand("/physical").isEmpty());
    }

    @Test
    @DisplayName("Сохраняет порядок регистрации конвертеров")
    void shouldKeepRegistrationOrder() {
        assertEquals(List.of("Валюты", "Длина", "Вес", "Температура"),
                registry.all().stream().map(QuantityConverter::title).toList());
    }
}
