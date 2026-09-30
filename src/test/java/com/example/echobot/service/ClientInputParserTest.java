package com.example.echobot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Map.Entry<CurrencyType, CurrencyType> result = clientInputParser
                .parse("567 USD to CNY", CurrencyType.class);
        assertEquals(CurrencyType.USD, result.getKey());
        assertEquals(CurrencyType.CNY, result.getValue());
    }

    @ParameterizedTest(name = "Вход: '{0}'")
    @ValueSource(strings = {
            "234 EuR to kzT",
            "763 eur to kzt",
    })
    @DisplayName("Корректно обрабатывает большие и маленькие буквы")
    void shouldParseValidMessagesWithLowercase(String input) {
        Map.Entry<CurrencyType, CurrencyType> result = clientInputParser.parse(input, CurrencyType.class);
        assertEquals(CurrencyType.EUR, result.getKey());
        assertEquals(CurrencyType.KZT, result.getValue());
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
        assertThrows(IllegalArgumentException.class, () -> {
            clientInputParser.parse(input, CurrencyType.class);
        });
    }

    @Test
    @DisplayName("Выбрасывает исключение для неизвестной валюты")
    void shouldThrowExceptionOnInvalidCurrency() {
        String input = "10 BTC to USD";

        assertThrows(IllegalArgumentException.class, () -> {
            clientInputParser.parse(input, CurrencyType.class);
        });
    }

    @Test
    @DisplayName("Выбрасывает исключение для опечатки в валюте")
    void shouldThrowExceptionOnTypo() {
        String input = "10 USDT to EUR";

        assertThrows(IllegalArgumentException.class, () -> {
            clientInputParser.parse(input, CurrencyType.class);
        });
    }

    @Test
    @DisplayName("Обработка null входных данных")
    void shouldHandleNullMessage() {
        assertThrows(NullPointerException.class, () -> {
            clientInputParser.parse(null, CurrencyType.class);
        });
    }
}
