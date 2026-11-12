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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты форматтера сообщений {@link BotMessageFormatter}.
 * Проверяют полные строки, которые пользователь получит в ответе бота.
 */
class BotMessageFormatterTest {

    /** Форматтер сообщений, создаваемый перед каждым тестом. */
    private BotMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new BotMessageFormatter();
    }

    /**
     * Проверяет приветственное сообщение полностью.
     */
    @Test
    @DisplayName("Возвращает приветствие на start()")
    void testStart() {
        var result = formatter.start();

        assertEquals("""
                Приветствую вас! Этот бот конвертирует валюты и физические величины.
                Чтобы узнать подробнее, введите /help
                """, result);
    }

    /**
     * Проверяет справочное сообщение полностью.
     */
    @Test
    @DisplayName("Возвращает справку на help()")
    void testHelp() {
        var result = formatter.help();

        assertEquals("""
                Формат запроса: {число} {единица1} to {единица2}
                Например: 100 USD to EUR
                Или: 10 KM to MI
                
                Доступные валюты: /currency
                Доступные величины:
                1) /length
                2) /weight
                3) /temperature
                """, result);
    }

    /**
     * Проверяет точность формата результата конвертации: 4 знака после запятой.
     */
    @Test
    @DisplayName("Форматирует результат с 4 знаками после запятой")
    void testGetResultFormat() throws InvalidUserInputException {
        var fromUnit = createMockQuantityType("USD");
        var toUnit = createMockQuantityType("EUR");
        var request = new ConversionRequest(
                BigDecimal.valueOf(100),
                fromUnit,
                toUnit
        );
        var result = BigDecimal.valueOf(92.5);

        var formatted = formatter.getResult(request, result);

        assertEquals("100.0000 USD = 92.5000 EUR", formatted);
    }

    /**
     * Проверяет использование точки как разделителя десятичных знаков (Locale.US).
     */
    @Test
    @DisplayName("Использует точку как разделитель десятичных знаков")
    void testGetResultUsesDotSeparator() throws InvalidUserInputException {
        var fromUnit = createMockQuantityType("KM");
        var toUnit = createMockQuantityType("MI");
        var request = new ConversionRequest(
                BigDecimal.valueOf(10.5),
                fromUnit,
                toUnit
        );
        var result = BigDecimal.valueOf(6.5242);

        var formatted = formatter.getResult(request, result);

        assertTrue(formatted.contains("."));
        assertFalse(formatted.contains(","));
        assertEquals("10.5000 KM = 6.5242 MI", formatted);
    }

    /**
     * Проверяет корректное форматирование нулевого значения.
     */
    @Test
    @DisplayName("Обрабатывает нулевое значение")
    void testGetResultWithZero() throws InvalidUserInputException {
        var fromUnit = createMockQuantityType("USD");
        var toUnit = createMockQuantityType("RUB");
        var request = new ConversionRequest(
                BigDecimal.ZERO,
                fromUnit,
                toUnit
        );

        var formatted = formatter.getResult(request, BigDecimal.ZERO);

        assertEquals("0.0000 USD = 0.0000 RUB", formatted);
    }

    /**
     * Проверяет корректное форматирование отрицательных значений.
     */
    @Test
    @DisplayName("Обрабатывает отрицательные значения")
    void testGetResultWithNegative() throws InvalidUserInputException {
        var fromUnit = createMockQuantityType("C");
        var toUnit = createMockQuantityType("F");
        var request = new ConversionRequest(
                BigDecimal.valueOf(-40),
                fromUnit,
                toUnit
        );

        var formatted = formatter.getResult(request, BigDecimal.valueOf(-40));

        assertEquals("-40.0000 C = -40.0000 F", formatted);
    }

    /**
     * Проверяет корректное форматирование больших чисел без потери точности.
     */
    @Test
    @DisplayName("Обрабатывает большие числа")
    void testGetResultWithLargeNumbers() throws InvalidUserInputException {
        var fromUnit = createMockQuantityType("CM");
        var toUnit = createMockQuantityType("M");
        var request = new ConversionRequest(
                BigDecimal.valueOf(1000000),
                fromUnit,
                toUnit
        );

        var formatted = formatter.getResult(request, BigDecimal.valueOf(10000));

        assertEquals("1000000.0000 CM = 10000.0000 M", formatted);
    }

    /**
     * Интеграционный тест: полное сообщение курсов валют с датой обновления.
     * Проверяет заголовок, все курсы и отформатированную дату.
     */
    @Test
    @DisplayName("Полное сообщение курсов валют с датой обновления")
    void testFullCurrencyRatesWithDate() {
        var baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new LinkedHashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));
        rates.put(createMockQuantityType("RUB"), BigDecimal.ONE);

        var lastUpdated = LocalDateTime.of(2024, 1, 15, 14, 30);

        var result = formatter.currencyRates(baseUnit, rates, lastUpdated);

        var expected = """
                Актуальные курсы валют:
                1 USD = 0.9250 EUR
                1 USD = 1.0000 RUB
                
                Последнее обновление: 15.01.2024 14:30""";
        assertEquals(expected, result);
    }

    /**
     * Интеграционный тест: полное сообщение курсов валют без даты обновления.
     */
    @Test
    @DisplayName("Полное сообщение курсов валют без даты обновления")
    void testFullCurrencyRatesWithoutDate() {
        var baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new LinkedHashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));

        var result = formatter.currencyRates(baseUnit, rates, null);

        var expected = """
                Актуальные курсы валют:
                1 USD = 0.9250 EUR
                
                Время последнего обновления неизвестно.""";
        assertEquals(expected, result);
    }

    /**
     * Интеграционный тест: полное сообщение справочника курсов для группы длины.
     */
    @Test
    @DisplayName("Полное сообщение справочника курсов для группы длины")
    void testFullRatesLengthGroup() {
        var baseUnit = createMockQuantityType("METER");
        Map<QuantityType, BigDecimal> rates = new LinkedHashMap<>();
        rates.put(createMockQuantityType("KILOMETER"), new BigDecimal("0.001"));
        rates.put(createMockQuantityType("MILE"), new BigDecimal("0.000621371"));
        rates.put(createMockQuantityType("CENTIMETER"), BigDecimal.valueOf(100));

        var result = formatter.rates("Величины длин", baseUnit, rates);

        var expected = """
                Величины длин:
                1 METER = 0.0010 KILOMETER
                1 METER = 0.0006 MILE
                1 METER = 100.0000 CENTIMETER
                """;
        assertEquals(expected, result);
    }

    /**
     * Интеграционный тест: полное сообщение справочника курсов для группы температуры.
     */
    @Test
    @DisplayName("Полное сообщение справочника курсов для группы температуры")
    void testFullRatesTemperatureGroup() {
        var baseUnit = createMockQuantityType("CELSIUS");
        Map<QuantityType, BigDecimal> rates = new LinkedHashMap<>();
        rates.put(createMockQuantityType("FAHRENHEIT"), new BigDecimal("1.8"));
        rates.put(createMockQuantityType("KELVIN"), BigDecimal.ONE);

        var result = formatter.rates("Величины температур", baseUnit, rates);

        var expected = """
                Величины температур:
                1 CELSIUS = 1.8000 FAHRENHEIT
                1 CELSIUS = 1.0000 KELVIN
                """;
        assertEquals(expected, result);
    }

    /**
     * Создаёт тестовый экземпляр {@link QuantityType} с заданным кодом.
     *
     * @param code строковый код единицы измерения или валюты
     * @return анонимная реализация интерфейса QuantityType
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
