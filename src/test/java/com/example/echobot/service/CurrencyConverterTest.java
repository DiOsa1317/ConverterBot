package com.example.echobot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CurrencyConverter.
 */
public class CurrencyConverterTest {

    private final ConverterService converterService = new ConverterService();

    /**
     * Проверяет конвертацию из иностранной валюты в рубли (умножение).
     * 10 USD * 84.3414 = 843.4140 RUB
     */
    @Test
    @DisplayName("Конвертация USD в RUB")
    void shouldConvertUsdToRub() {
        String result = converterService.processBotResponse("10 USD to RUB");
        assertTrue(result.contains("843.4140 RUB"));
    }

    /**
     * Проверяет конвертацию из рублей в иностранную валюту (деление).
     * 84.3414 RUB / 84.3414 = 1.0000 USD
     */
    @Test
    @DisplayName("Конвертация RUB в USD")
    void shouldConvertRubToUsd() {
        String result = converterService.processBotResponse("84.3414 RUB to USD");
        assertTrue(result.contains("1.0000 USD"));
    }

    /**
     * Проверяет кросс-курс (через рубль).
     * 100 USD -> RUB (8434.14) -> KZT (8434.14 / 1.91 ≈ 4415.7801)
     */
    @Test
    @DisplayName("Кросс-курс USD в KZT")
    void shouldConvertUsdToKzt() {
        String result = converterService.processBotResponse("100 USD to KZT");
        assertTrue(result.startsWith("100.0000 USD = "));
        assertTrue(result.contains("KZT"));
    }

    /**
     * Проверяет, что при конвертации валюты в саму себя сумма не меняется.
     */
    @Test
    @DisplayName("Конвертация EUR в EUR (без изменений)")
    void shouldHandleSameCurrency() {
        String result = converterService.processBotResponse("50 EUR to EUR");
        assertTrue(result.contains("50.0000 EUR = 50.0000 EUR"));
    }

    /**
     * Проверяет форматирование числа (ровно 4 знака после запятой).
     */
    @Test
    @DisplayName("Проверка формата вывода (4 знака после запятой)")
    void shouldFormatOutputCorrectly() {
        String result = converterService.processBotResponse("1 CNY to RUB");
        // 1 * 12.5355 = 12.5355
        assertEquals("1.0000 CNY = 12.5355 RUB", result.trim());
    }
}