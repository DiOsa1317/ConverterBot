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
import ru.urfu.converterbot.service.CustomConversionRepository;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        var repo = new CustomConversionRepository(null) {
            @Override
            public void save(CustomConversion conversion) {}
            @Override
            public Optional<CustomConversion> findByChatidFromAndAndTo(long chatId, String from, String to) {
                return Optional.empty();
            }
            @Override
            public List<CustomConversion> findAllOutgoing(long chatId) {
                return List.of();
            }
            @Override
            public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) {
                return false;
            }
            @Override
            public List<String> findAllUnitNames(long chatId) {
                return List.of();
            }
            @Override
            public boolean existsByChatIdAndFromAndTo(long chatId, String from, String to) {
                return false;
            }
        };
        var parser = new ClientInputParser(repo);
        var formatter = new BotMessageFormatter();
        var lengthConverter = new LengthConverter(new LengthData());
        var weightConverter = new WeightConverter(new WeightData());
        var currencyConverter = new CurrencyConverter(new CurrencyData(), repo);
        var temperatureConverter = new TemperatureConverter(new TemperatureData());
        botService = new BotService(
                currencyConverter,
                lengthConverter,
                weightConverter,
                temperatureConverter,
                parser,
                formatter);
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
        assertEquals(expected, botService.processBotResponse(request));
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
        assertTrue(botService.processBotResponse(command).contains(expectedFragment));
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
        assertDoesNotThrow(() -> botService.processBotResponse(message));
    }

    /**
     * Проверяет, что не-текстовое сообщение тоже обрабатывается без исключения.
     */
    @Test
    @DisplayName("Не выбрасывает исключение на null")
    void shouldNotThrowOnNull() {
        assertDoesNotThrow(() -> botService.processBotResponse(null));
    }
}