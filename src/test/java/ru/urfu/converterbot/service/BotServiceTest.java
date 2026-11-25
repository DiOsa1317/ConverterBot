package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.CustomConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.converters.models.LengthData;
import ru.urfu.converterbot.service.converters.models.TemperatureData;
import ru.urfu.converterbot.service.converters.models.WeightData;
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
        var currencyConverter = new CurrencyConverter(new CurrencyData());
        var customConverter = new CustomConverter(customRepo);
        var temperatureConverter = new TemperatureConverter(new TemperatureData());
        botService = new BotService(
                currencyConverter,
                lengthConverter,
                weightConverter,
                temperatureConverter,
                customConverter,
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
     */
    @Test
    @DisplayName("Возвращает справку по валютам")
    void shouldReturnCurrencyHelp() {
        assertEquals("Актуальные курсы валют:\n1 RUB = 0.0104 EUR\n1 RUB = 0.0119 USD\n1 RUB = 0.0798 CNY\n1 RUB = 0.5236 KZT\n\nВремя последнего обновления неизвестно.\n",
                botService.processBotResponse(1, "/currency"));
    }

    @Test
    @DisplayName("Возвращает справку по длинам")
    void shouldReturnLengthHelp() {
        assertEquals("Величины длины:\n1 m = 0.0010 km\n1 m = 0.0006 mi\n1 m = 100.0000 cm\n1 m = 39.3701 in\n",
                botService.processBotResponse(1, "/length"));
    }

    @Test
    @DisplayName("Возвращает справку по весам")
    void shouldReturnWeightHelp() {
        assertEquals("Величины веса:\n1 kg = 1000.0000 g\n1 kg = 0.0010 t\n1 kg = 2.2046 lb\n",
                botService.processBotResponse(1, "/weight"));
    }

    @Test
    @DisplayName("Возвращает справку по температурам")
    void shouldReturnTemperatureHelp() {
        assertEquals("Величины температур:\n1 °C = 1.0000 °C\n1 °C = 33.8000 °F\n1 °C = 274.1500 K\n",
                botService.processBotResponse(1, "/temperature"));
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

}