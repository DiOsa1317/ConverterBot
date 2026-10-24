package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.BotCommand;
import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для BotMessageFormatter.
 */
class BotMessageFormatterTest {

    private BotMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new BotMessageFormatter();
    }

    @Test
    @DisplayName("Возвращает приветствие на start()")
    void testStart() {
        String result = formatter.start();

        assertNotNull(result);
        assertTrue(result.contains("Приветствую вас"));
        assertTrue(result.contains("конвертирует валюты и физические величины"));
        assertTrue(result.contains("/help"));
    }

    @Test
    @DisplayName("Возвращает справку на help()")
    void testHelp() {
        String result = formatter.help();

        assertNotNull(result);
        assertTrue(result.contains("Формат запроса"));
        assertTrue(result.contains("{число} {единица1} to {единица2}"));
        assertTrue(result.contains("100 USD to EUR"));
        assertTrue(result.contains("10 KM to MI"));
        assertTrue(result.contains("/currency"));
        assertTrue(result.contains("/length"));
        assertTrue(result.contains("/weight"));
        assertTrue(result.contains("/temperature"));
    }

    @Test
    @DisplayName("Возвращает сообщение о нетекстовом вводе")
    void testNoText() {
        String result = formatter.noText();

        assertNotNull(result);
        assertTrue(result.contains("только текстовые сообщения"));
        assertTrue(result.contains("/help"));
    }

    @Test
    @DisplayName("Возвращает сообщение о неверном формате ввода")
    void testInvalidInput() {
        String result = formatter.invalidInput();

        assertNotNull(result);
        assertTrue(result.contains("Не удалось разобрать запрос"));
        assertTrue(result.contains("{число} {единица1} to {единица2}"));
        assertTrue(result.contains("/help"));
    }

    @Test
    @DisplayName("Возвращает сообщение о непредвиденной ошибке")
    void testUnexpectedError() {
        String result = formatter.unexpectedError();

        assertNotNull(result);
        assertTrue(result.contains("непредвиденная ошибка"));
        assertTrue(result.contains("Попробуйте ещё раз"));
    }


    @Test
    @DisplayName("Форматирует результат с 4 знаками после запятой")
    void testGetResultFormat() throws InvalidUserInputException {
        QuantityType fromUnit = createMockQuantityType("USD");
        QuantityType toUnit = createMockQuantityType("EUR");
        ConversionRequest request = new ConversionRequest(
                BigDecimal.valueOf(100),
                fromUnit,
                toUnit
        );
        BigDecimal result = BigDecimal.valueOf(92.5);

        String formatted = formatter.getResult(request, result);

        assertEquals("100.0000 USD = 92.5000 EUR", formatted);
    }

    @Test
    @DisplayName("Использует точку как разделитель десятичных знаков")
    void testGetResultUsesDotSeparator() throws InvalidUserInputException {
        QuantityType fromUnit = createMockQuantityType("KM");
        QuantityType toUnit = createMockQuantityType("MI");
        ConversionRequest request = new ConversionRequest(
                BigDecimal.valueOf(10.5),
                fromUnit,
                toUnit
        );
        BigDecimal result = BigDecimal.valueOf(6.5242);

        String formatted = formatter.getResult(request, result);

        // Проверяем, что используется точка, а не запятая
        assertTrue(formatted.contains("."));
        assertFalse(formatted.contains(","));
        assertEquals("10.5000 KM = 6.5242 MI", formatted);
    }

    @Test
    @DisplayName("Обрабатывает нулевое значение")
    void testGetResultWithZero() throws InvalidUserInputException {
        QuantityType fromUnit = createMockQuantityType("USD");
        QuantityType toUnit = createMockQuantityType("RUB");
        ConversionRequest request = new ConversionRequest(
                BigDecimal.ZERO,
                fromUnit,
                toUnit
        );

        String formatted = formatter.getResult(request, BigDecimal.ZERO);

        assertEquals("0.0000 USD = 0.0000 RUB", formatted);
    }

    @Test
    @DisplayName("Обрабатывает отрицательные значения")
    void testGetResultWithNegative() throws InvalidUserInputException {
        QuantityType fromUnit = createMockQuantityType("C");
        QuantityType toUnit = createMockQuantityType("F");
        ConversionRequest request = new ConversionRequest(
                BigDecimal.valueOf(-40),
                fromUnit,
                toUnit
        );

        String formatted = formatter.getResult(request, BigDecimal.valueOf(-40));

        assertEquals("-40.0000 C = -40.0000 F", formatted);
    }

    @Test
    @DisplayName("Обрабатывает большие числа")
    void testGetResultWithLargeNumbers() throws InvalidUserInputException {
        QuantityType fromUnit = createMockQuantityType("CM");
        QuantityType toUnit = createMockQuantityType("M");
        ConversionRequest request = new ConversionRequest(
                BigDecimal.valueOf(1000000),
                fromUnit,
                toUnit
        );

        String formatted = formatter.getResult(request, BigDecimal.valueOf(10000));

        assertEquals("1000000.0000 CM = 10000.0000 M", formatted);
    }

    @Test
    @DisplayName("Форматирует курсы валют с датой обновления")
    void testCurrencyRatesWithDate() {
        QuantityType baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));

        LocalDateTime lastUpdated = LocalDateTime.of(2024, 1, 15, 14, 30);

        String result = formatter.currencyRates(baseUnit, rates, lastUpdated);

        assertTrue(result.contains("Курсы валют ЦБ РФ:"));
        assertTrue(result.contains("1 USD = 0.9250 EUR"));
        assertTrue(result.contains("Последнее обновление: 15.01.2024 14:30"));
    }

    @Test
    @DisplayName("Форматирует курсы валют без даты обновления")
    void testCurrencyRatesWithoutDate() {
        QuantityType baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));

        String result = formatter.currencyRates(baseUnit, rates, null);

        assertTrue(result.contains("Курсы валют ЦБ РФ:"));
        assertTrue(result.contains("1 USD = 0.9250 EUR"));
        assertTrue(result.contains("Время последнего обновления неизвестно."));
    }

    @Test
    @DisplayName("Проверяет формат даты dd.MM.yyyy HH:mm")
    void testCurrencyRatesDateFormat() {
        QuantityType baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.ONE);

        LocalDateTime testDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        String result = formatter.currencyRates(baseUnit, rates, testDate);

        assertTrue(result.contains("31.12.2024 23:59"));
    }

    /**
     * Создает mock QuantityType для тестов
     */
    private QuantityType createMockQuantityType(String code) {
        return new QuantityType() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public List<String> getCodes() {
                return List.of();
            }

            @Override
            public BotCommand group() {
                return null;
            }


            @Override
            public String toString() {
                return code;
            }
        };
    }
}