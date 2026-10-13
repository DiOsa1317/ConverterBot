package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ConverterService
 */
public class ConverterServiceTest {

    /**
     * Диспетчер перевода, создаваемый заново перед каждым тестом.
     */
    private ConverterService converterService;

    /**
     * Создаёт реестр со всеми конвертерами и диспетчер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        var registry = new ConverterRegistry();
        var parser = new ClientInputParser();
        converterService = new ConverterService(registry, parser);
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
        assertEquals(expected, converterService.processBotResponse(request));
    }

    /**
     * Проверяет, что на команду {@code /start} возвращается приветствие.
     */
    @Test
    @DisplayName("Отвечает приветствием на /start")
    void shouldReturnStartText() {
        assertTrue(converterService.processBotResponse("/start").contains("Приветствую"));
    }

    /**
     * Проверяет, что на команду {@code /help} возвращается справка.
     */
    @Test
    @DisplayName("Отвечает справкой на /help")
    void shouldReturnHelpText() {
        assertTrue(converterService.processBotResponse("/help").contains("Формат запроса"));
    }

    /**
     * Проверяет, что каждая команда группы возвращает справку именно своей группы.
     *
     * @param command          команда справки по группе величин
     * @param expectedFragment фрагмент, который обязан быть в справке группы
     */
    @ParameterizedTest(name = "Команда {0}")
    @CsvSource({
            "/currency, Доступны переводы",
            "/length, Величины длин",
            "/weight, Величины весов",
            "/temperature, температур"
    })
    @DisplayName("Возвращает справку по группе на её команду")
    void shouldReturnGroupHelp(String command, String expectedFragment) {
        assertTrue(converterService.processBotResponse(command).contains(expectedFragment));
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
        assertDoesNotThrow(() -> converterService.processBotResponse(message));
    }

    /**
     * Проверяет, что не-текстовое сообщение тоже обрабатывается без исключения.
     */
    @Test
    @DisplayName("Не выбрасывает исключение на null")
    void shouldNotThrowOnNull() {
        assertDoesNotThrow(() -> converterService.processBotResponse(null));
    }
}