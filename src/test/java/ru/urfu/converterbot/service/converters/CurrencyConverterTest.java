package ru.urfu.converterbot.service.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.models.CustomQuantityType;
import ru.urfu.converterbot.service.models.fakeClasses.FakeCustomConversionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        currencyConverter = new CurrencyConverter(new CurrencyData());
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
    @DisplayName("Правильно конвертирует валюты")
    void shouldConvertCurrency(BigDecimal value, String from, String to, BigDecimal expected) {
        BigDecimal result = currencyConverter.convert(value, CurrencyType.valueOf(from), CurrencyType.valueOf(to));
        assertEquals(0, expected.compareTo(result),
                () -> "Ожидалось: " + expected + ", но получено: " + result);
    }

    /*
    * Проверяет, что конвертер конвертирует пользовательские единицы напрямую.
     */
    @Test
    @DisplayName("Конвертирует пользовательские единицы напрямую")
    void shouldConvertCustomUnitsDirectly() {
        var repository = new FakeCustomConversionRepository();

        repository.save(new CustomConversion(
                "kk", "mm", new BigDecimal("2"), 1L
        ));

        var converter = new CurrencyConverter(new CurrencyData(), repository);

        var result = converter.convert(
                new BigDecimal("5"),
                new CustomQuantityType("kk", 1L),
                new CustomQuantityType("mm", 1L)
        );

        assertEquals(0, new BigDecimal("10.0000000000").compareTo(result));
    }

    /*
    * Проверяет, что конвертер конвертирует пользовательские единицы в обратном направлении.
     */
    @Test
    @DisplayName("Конвертирует пользовательские единицы в обратном направлении")
    void shouldConvertCustomUnitsInReverse() {
        var repository = new FakeCustomConversionRepository();

        repository.save(new CustomConversion(
                "kk", "mm", new BigDecimal("2"), 1L
        ));

        var converter = new CurrencyConverter(new CurrencyData(), repository);

        var result = converter.convert(
                new BigDecimal("10"),
                new CustomQuantityType("mm", 1L),
                new CustomQuantityType("kk", 1L)
        );

        assertEquals(0, new BigDecimal("5.0000000000").compareTo(result));
    }

    /* 
    * Проверяет, что конвертер не использует пользовательский курс другого чата.
     */
    @Test
    @DisplayName("Не использует пользовательский курс другого чата")
    void shouldNotUseCustomRateFromAnotherChat() {
        var repository = new FakeCustomConversionRepository();

        repository.save(new CustomConversion(
                "kk", "mm", new BigDecimal("2"), 1L
        ));

        var converter = new CurrencyConverter(new CurrencyData(), repository);

        assertThrows(
                InvalidUserInputException.class,
                () -> converter.convert(
                        new BigDecimal("5"),
                        new CustomQuantityType("kk", 2L),
                        new CustomQuantityType("mm", 2L)
                )
        );
    }

    /*
    * Проверяет, что конвертер выбрасывает ошибку, если пользовательский курс не найден.
     */
    @Test
    @DisplayName("Выбрасывает ошибку, если пользовательский курс не найден")
    void shouldThrowExceptionWhenCustomRateNotFound() {
        var repository = new FakeCustomConversionRepository();
        var converter = new CurrencyConverter(new CurrencyData(), repository);

        assertThrows(
                InvalidUserInputException.class,
                () -> converter.convert(
                        new BigDecimal("5"),
                        new CustomQuantityType("kk", 1L),
                        new CustomQuantityType("mm", 1L)
                )
        );
    }
}