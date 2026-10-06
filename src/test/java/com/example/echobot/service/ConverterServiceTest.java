package com.example.echobot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterServiceTest {

    private ConverterService converterService;

    @BeforeEach
    void setUp() {
        var registry = new ConverterRegistry(List.of(
                new CurrencyConverter(),
                new LengthConverter(),
                new WeightConverter(),
                new TemperatureConverter()
        ));
        converterService = new ConverterService(registry);
    }

    @Test
    @DisplayName("Форматирует результат четырьмя знаками после запятой")
    void shouldFormatResult() {
        assertEquals("10.0000 USD = 843.4140 RUB",
                converterService.convert("10.000 USD to RUB"));
    }

    @ParameterizedTest(name = "Команда: '{0}")
    @ValueSource(strings = {"/currency", "/length", "/weight", "/temperature"})
    @DisplayName("Возвращает справку по каждой группе величин")
    void shouldReturnHelpByCommand(String command) {
        assertFalse(converterService.getHelp(command).isBlank());
    }

    @Test
    @DisplayName("Отказывает в переводе между разными группами величин")
    void shouldRejectConversionBetweenDifferentGroups() {
        var exception = assertThrows(ConversionException.class,
                () -> converterService.convert("10 KILOMETER to KILOGRAM"));
        assertFalse(exception.getMessage().isBlank());
    }

    @ParameterizedTest(name = "Мусор: '{0}")
    @ValueSource(strings = {"привет", "10 USD to", "10 BTC to USD", "abc USD to EUR"})
    @DisplayName("Отвечает понятной ошибкой на некорректный ввод")
    void shouldFailWithRussianMessageBadInput(String message) {
        var exception = assertThrows(ConversionException.class,
                () -> converterService.convert(message));
        assertTrue(exception.getMessage().matches(".*[а-яА-Я].*"),
                "Сообщение должно быть на русском: " + exception.getMessage());
    }

}