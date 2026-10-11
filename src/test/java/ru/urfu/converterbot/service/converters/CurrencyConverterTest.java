package ru.urfu.converterbot.service.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.CurrencyType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @ParameterizedTest(name = "{0} {1} -> {2} = {3}")
    @CsvSource({
            "10,      USD, RUB, 843.414",
            "84.3414, RUB, USD, 1",
            "100,     USD, KZT, 4415.7801047120",
            "1,       CNY, RUB, 12.5355",
            "50,      EUR, EUR, 50"
    })
    @DisplayName("Конвертирует валюты через рубль как базовую")
    void shouldConvertCurrency(BigDecimal value, String from, String to, BigDecimal expected) {
        var request = new ConversionRequest(value,
                CurrencyType.valueOf(from), CurrencyType.valueOf(to));
        BigDecimal result = currencyConverter.convert(request);
        assertEquals(0, expected.compareTo(result),
                () -> "Ожидалось: " + expected + ", но получено: " + result);
    }
}