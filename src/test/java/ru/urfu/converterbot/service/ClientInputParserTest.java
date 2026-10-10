package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.exceptions.ConversionException;
import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.TemperatureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ru.urfu.converterbot.service.models.CurrencyType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ClientInputParser.
 */
public class ClientInputParserTest {
    /** Экземпляр парсера, создаваемый заново перед каждым тестом. */
    private ClientInputParser clientInputParser;

    /**
     * Создаёт новый парсер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        clientInputParser = new ClientInputParser();
    }

    /**
     * Проверяет, что валюты корректно извлекаются из входного сообщения.
     */
    @Test
    @DisplayName("Правильно извлек валюты из текста пользователя")
    void shouldParseValidMessagesWithCurrency() {
        ConversionRequest result = clientInputParser
                .parse("567 USD to CNY");
        assertEquals(CurrencyType.USD, result.from());
        assertEquals(CurrencyType.CNY, result.to());
    }

    /**
     * Проверяет, что регистр букв в единицах измерения не важен.
     *
     * @param input входное сообщение, записанное в смешанном регистре
     */
    @ParameterizedTest(name = "Вход: '{0}'")
    @ValueSource(strings = {
            "234 EuR to kzT",
            "763 eur to kzt",
    })
    @DisplayName("Считывает названия величин вне зависимости от регистра")
    void shouldParseValidMessagesWithLowercase(String input) {
        ConversionRequest result = clientInputParser.parse(input);
        assertEquals(CurrencyType.EUR, result.from());
        assertEquals(CurrencyType.KZT, result.to());
    }


    /**
     * Проверяет, что при неверном количестве слов бросается исключение.
     *
     * @param input входное сообщение со структурой, отличной от формата парсера
     */
    @ParameterizedTest(name = "Неверная длина: '{0}'")
    @ValueSource(strings = {
            "10 USD to",
            "10 USD to EUR now",
            "USD",
            ""
    })
    @DisplayName("Выбрасывает исключение при неверном количестве слов")
    void shouldThrowExceptionOnWrongLength(String input) {
        assertThrows(IllegalArgumentException.class, () -> clientInputParser.parse(input));
    }

    /**
     * Проверяет, что неизвестная валюта отклоняется парсером.
     */
    @Test
    @DisplayName("Выбрасывает исключение для неизвестной валюты")
    void shouldThrowExceptionOnInvalidCurrency() {
        String input = "10 BTC to USD";

        assertThrows(IllegalArgumentException.class, () -> clientInputParser.parse(input));
    }

    /**
     * Проверяет поведение парсера при {@code null} вместо текста сообщения.
     */
    @Test
    @DisplayName("Обработка null входных данных")
    void shouldHandleNullMessage() {
        assertThrows(NullPointerException.class, () -> clientInputParser.parse(null));
    }

    /**
     * Проверяет, что разделителем между единицами может быть только слово {@code to}.
     */
    @Test
    @DisplayName("Выбрасывает исключение, если разделитель не равен to")
    void shouldThrowExceptionOnWrongSeparator() {
        assertThrows(ConversionException.class,
                () -> clientInputParser.parse("10 USD XX EUR"));
    }

    /**
     * Проверяет, что единицы физических величин распознаются
     * наравне с валютами.
     */
    @Test
    @DisplayName("Разбирает единицы физических величин")
    void shouldParsePhysicalUnits() {
        var result = clientInputParser.parse("20 CELSIUS to FAHRENHEIT");
        assertEquals(TemperatureType.CELSIUS, result.from());
        assertEquals(TemperatureType.FAHRENHEIT, result.to());
    }
}
