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
 * Тесты контракта форматтера сообщений {@link BotMessageFormatter}.
 */
class BotMessageFormatterTest {

    private BotMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new BotMessageFormatter();
    }

    /**
     * Проверяет наличие обязательных элементов в приветственном сообщении.
     */
    @Test
    @DisplayName("Возвращает приветствие на start()")
    void testStart() {
        var result = formatter.start();

        assertNotNull(result);
        assertTrue(result.contains("Приветствую вас"));
        assertTrue(result.contains("конвертирует валюты и физические величины"));
        assertTrue(result.contains("/help"));
    }

    /**
     * Проверяет полноту справочного сообщения: формат запроса, примеры и доступные команды.
     */
    @Test
    @DisplayName("Возвращает справку на help()")
    void testHelp() {
        var result = formatter.help();

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

    /**
     * Проверяет сообщение-заглушку для нетекстового ввода.
     */
    @Test
    @DisplayName("Возвращает сообщение о нетекстовом вводе")
    void testNoText() {
        var result = formatter.noText();

        assertNotNull(result);
        assertTrue(result.contains("только текстовые сообщения"));
        assertTrue(result.contains("/help"));
    }

    /**
     * Проверяет сообщение об ошибке при неверном формате запроса на конвертацию.
     */
    @Test
    @DisplayName("Возвращает сообщение о неверном формате ввода")
    void testInvalidInput() {
        var result = formatter.invalidInput();

        assertNotNull(result);
        assertTrue(result.contains("Не удалось разобрать запрос"));
        assertTrue(result.contains("{число} {единица1} to {единица2}"));
        assertTrue(result.contains("/help"));
    }

    /**
     * Проверяет сообщение-заглушку для непредвиденных ошибок.
     */
    @Test
    @DisplayName("Возвращает сообщение о непредвиденной ошибке")
    void testUnexpectedError() {
        var result = formatter.unexpectedError();

        assertNotNull(result);
        assertTrue(result.contains("непредвиденная ошибка"));
        assertTrue(result.contains("Попробуйте ещё раз"));
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
     * Проверяет форматирование списка курсов валют с указанием даты обновления.
     */
    @Test
    @DisplayName("Форматирует курсы валют с датой обновления")
    void testCurrencyRatesWithDate() {
        var baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));

        var lastUpdated = LocalDateTime.of(2024, 1, 15, 14, 30);

        var result = formatter.currencyRates(baseUnit, rates, lastUpdated);

        assertTrue(result.contains("Курсы валют ЦБ РФ:"));
        assertTrue(result.contains("1 USD = 0.9250 EUR"));
        assertTrue(result.contains("Последнее обновление: 15.01.2024 14:30"));
    }

    /**
     * Проверяет форматирование списка курсов валют при отсутствии даты обновления.
     */
    @Test
    @DisplayName("Форматирует курсы валют без даты обновления")
    void testCurrencyRatesWithoutDate() {
        var baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.valueOf(0.925));

        var result = formatter.currencyRates(baseUnit, rates, null);

        assertTrue(result.contains("Курсы валют ЦБ РФ:"));
        assertTrue(result.contains("1 USD = 0.9250 EUR"));
        assertTrue(result.contains("Время последнего обновления неизвестно."));
    }

    /**
     * Проверяет соответствие формата даты шаблону dd.MM.yyyy HH:mm.
     */
    @Test
    @DisplayName("Проверяет формат даты dd.MM.yyyy HH:mm")
    void testCurrencyRatesDateFormat() {
        var baseUnit = createMockQuantityType("USD");
        Map<QuantityType, BigDecimal> rates = new HashMap<>();
        rates.put(createMockQuantityType("EUR"), BigDecimal.ONE);

        var testDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        var result = formatter.currencyRates(baseUnit, rates, testDate);

        assertTrue(result.contains("31.12.2024 23:59"));
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