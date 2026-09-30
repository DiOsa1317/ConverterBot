package com.example.echobot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientInputParserTest {
    private ClientInputParser clientInputParser;

    @BeforeEach
    void setUp() {
        clientInputParser = new ClientInputParser();
    }

    @Test
    @DisplayName("Корректно извлекает валюты из ввода")
    void shouldParseValidMessagesWithCurrency() {
        Map.Entry<CurrencyType, CurrencyType> result = clientInputParser.parse("567 USD to CNY", CurrencyType.class);
        assertEquals(CurrencyType.USD, result.getKey());
        assertEquals(CurrencyType.CNY, result.getValue());
    }
}
