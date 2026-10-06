package com.example.echobot.service;

import com.example.echobot.service.models.TemperatureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.QuantityModel;

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
    @DisplayName("Корректно извлекает валюты из ввода")
    void shouldParseValidMessagesWithCurrency() {
        QuantityModel result = clientInputParser
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
    @DisplayName("Корректно обрабатывает большие и маленькие буквы")
    void shouldParseValidMessagesWithLowercase(String input) {
        QuantityModel result = clientInputParser.parse(input);
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
     * Проверяет, что опечатка в названии валюты отклоняется парсером.
     */
    @Test
    @DisplayName("Выбрасывает исключение для опечатки в валюте")
    void shouldThrowExceptionOnTypo() {
        String input = "10 USDT to EUR";

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
     * Проверяет, что нечисловое значение приводит к сообщению на русском языке.
     */
    @Test
    @DisplayName("Выбрасывает исключение, если первое слово не число")
    void shouldThrowExceptionOnNonNumericValue() {
        var exception = assertThrows(ConversionException.class,
                () -> clientInputParser.parse("abc USD to EUR"));
        assertTrue(exception.getMessage().contains("числом"), exception.getMessage());
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
