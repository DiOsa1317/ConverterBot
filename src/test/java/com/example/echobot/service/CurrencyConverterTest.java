package com.example.echobot.service;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.QuantityModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CurrencyConverter.
 */
public class CurrencyConverterTest {

    /** Экземпляр конвертера валют, создаваемый заново перед каждым тестом. */
    private CurrencyConverter currencyConverter;

    /**
     * Создаёт конвертер валют перед каждым тестом.
     */
    @BeforeEach
    public void setUp() {
        currencyConverter = new CurrencyConverter();
    }

    /**
     * Проверяет перевод валют через рубль как базовую, включая перевод
     * валюты в саму себя.
     *
     * @param value    исходное значение
     * @param from     имя исходной валюты
     * @param to       имя целевой валюты
     * @param expected ожидаемый результат перевода
     */
    @ParameterizedTest(name = "{0} {1} = {2}")
    @CsvSource({
            "10,      USD, RUB, 843.414",
            "84.3414, RUB, USD, 1",
            "100,     USD, KZT, 4415.780104712041",
            "1,       CNY, RUB, 12.5355",
            "50,      EUR, EUR, 50"
    })
    @DisplayName("Конвертирует валюты через рубль как базовую")
    void shouldConvertCurrency(double value, String from, String to, double expected) {
        var request = new QuantityModel(value,
                CurrencyType.valueOf(from), CurrencyType.valueOf(to));
        assertEquals(expected, currencyConverter.convert(request), 1e-9);
    }
}