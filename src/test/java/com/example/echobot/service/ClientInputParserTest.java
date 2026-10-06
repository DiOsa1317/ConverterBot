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
    private ClientInputParser clientInputParser;

    @BeforeEach
    void setUp() {
        clientInputParser = new ClientInputParser();
    }

    @Test
    @DisplayName("Корректно извлекает валюты из ввода")
    void shouldParseValidMessagesWithCurrency() {
        QuantityModel result = clientInputParser
                .parse("567 USD to CNY");
        assertEquals(CurrencyType.USD, result.from());
        assertEquals(CurrencyType.CNY, result.to());
    }

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

    @Test
    @DisplayName("Выбрасывает исключение для неизвестной валюты")
    void shouldThrowExceptionOnInvalidCurrency() {
        String input = "10 BTC to USD";

        assertThrows(IllegalArgumentException.class, () -> clientInputParser.parse(input));
    }

    @Test
    @DisplayName("Выбрасывает исключение для опечатки в валюте")
    void shouldThrowExceptionOnTypo() {
        String input = "10 USDT to EUR";

        assertThrows(IllegalArgumentException.class, () -> clientInputParser.parse(input));
    }

    @Test
    @DisplayName("Обработка null входных данных")
    void shouldHandleNullMessage() {
        assertThrows(NullPointerException.class, () -> clientInputParser.parse(null));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если разделитель не равен to")
    void shouldThrowExceptionOnWrongSeparator() {
        assertThrows(ConversionException.class,
                () -> clientInputParser.parse("10 USD XX EUR"));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если первое слово не число")
    void shouldThrowExceptionOnNonNumericValue() {
        var exception = assertThrows(ConversionException.class,
                () -> clientInputParser.parse("abc USD to EUR"));
        assertTrue(exception.getMessage().contains("числом"), exception.getMessage());
    }

    @Test
    @DisplayName("Разбирает единицы физических величин")
    void shouldParsePhysicalUnits() {
        var result = clientInputParser.parse("20 CELSIUS to FAHRENHEIT");
        assertEquals(TemperatureType.CELSIUS, result.from());
        assertEquals(TemperatureType.FAHRENHEIT, result.to());
    }
}
