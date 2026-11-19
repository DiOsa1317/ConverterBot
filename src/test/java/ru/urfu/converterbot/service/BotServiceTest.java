package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.converters.models.LengthData;
import ru.urfu.converterbot.service.converters.models.TemperatureData;
import ru.urfu.converterbot.service.converters.models.WeightData;
import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.models.CustomQuantityType;
import ru.urfu.converterbot.service.models.fakeClasses.FakeConversionHistoryRepository;
import ru.urfu.converterbot.service.models.fakeClasses.FakeCustomConversionRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Тесты для ConverterService
 */
public class BotServiceTest {

    /**
     * Диспетчер перевода, создаваемый заново перед каждым тестом.
     */
    private BotService botService;

    /**
     * Создаёт реестр со всеми конвертерами и диспетчер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        var customRepo = new FakeCustomConversionRepository();
        var historyRepo = new FakeConversionHistoryRepository();
        var parser = new ClientInputParser(customRepo);
        var formatter = new BotMessageFormatter();
        var lengthConverter = new LengthConverter(new LengthData());
        var weightConverter = new WeightConverter(new WeightData());
        var currencyConverter = new CurrencyConverter(new CurrencyData(), customRepo);
        var temperatureConverter = new TemperatureConverter(new TemperatureData());
        botService = new BotService(
                currencyConverter,
                lengthConverter,
                weightConverter,
                temperatureConverter,
                parser,
                formatter,
                historyRepo);
    }

    /**
     * Проверяет формат ответа на успешный перевод: 4 знака после запятой
     * и короткие коды единиц.
     *
     * @param request  сообщение пользователя в формате "{число} {из} to {в}"
     * @param expected ожидаемая строка ответа
     */
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "10.000 USD to RUB, 10.0000 USD = 843.4140 RUB",
            "100 CENTIMETER to METER, 100.0000 cm = 1.0000 m",
            "100 CELSIUS to FAHRENHEIT, 100.0000 °C = 212.0000 °F"
    })
    @DisplayName("Форматирует результат четырьмя знаками после запятой")
    void shouldFormatResult(String request, String expected) {
        assertEquals(expected, botService.processBotResponse(1, request));
    }

    /**
     * Проверяет, что каждая команда группы возвращает справку именно своей группы.
     *
     * @param command          команда справки по группе величин
     * @param expectedFragment фрагмент, который обязан быть в справке группы
     */
    @ParameterizedTest(name = "Команда {0}")
    @CsvSource({
            "/currency, Актуальные курсы валют",
            "/length, Величины длин",
            "/weight, Величины веса",
            "/temperature, температур"
    })
    @DisplayName("Возвращает справку по группе на её команду")
    void shouldReturnGroupHelp(String command, String expectedFragment) {
        assertTrue(botService.processBotResponse(1, command).contains(expectedFragment));
    }

    /**
     * Проверяет гарантию {@link BotResponseProcessor}: на любой некорректный
     * ввод диспетчер возвращает ответ, а не выбрасывает исключение.
     *
     * @param message некорректное сообщение пользователя
     */
    @ParameterizedTest(name = "Некорректный ввод: ''{0}''")
    @ValueSource(strings = {
            "привет",
            "10 USD to",
            "10 BTC to USD",
            "abc USD to EUR",
            "10 KILOMETER to KILOGRAM"
    })
    @DisplayName("Не выбрасывает исключение на некорректный ввод")
    void shouldNotThrowOnInvalidInput(String message) {
        assertDoesNotThrow(() -> botService.processBotResponse(1, message));
    }

    /**
     * Проверяет, что не-текстовое сообщение тоже обрабатывается без исключения.
     */
    @Test
    @DisplayName("Не выбрасывает исключение на null")
    void shouldNotThrowOnNull() {
        assertDoesNotThrow(() -> botService.processBotResponse(1, null));
    }

    /**
     * Проверяет на успешный перевод одной кастомной единицы в другую.
     */
    @Test
    @DisplayName("Переводит одну кастомную единицу в другую")
    void shouldConvertCustomUnits() {
        var customRepo = new FakeCustomConversionRepository();

        customRepo.saveOrUpdate(new CustomConversion(
                "kk",
                "mm",
                new BigDecimal("2"),
                1L
        ));

        var converter = new CurrencyConverter(new CurrencyData(), customRepo);

        var from = new CustomQuantityType("kk", 1L);
        var to = new CustomQuantityType("mm", 1L);

        var result = converter.convert(
                new BigDecimal("5"),
                from,
                to
        );

        assertEquals(new BigDecimal("10.0000000000"), result);
    }
}