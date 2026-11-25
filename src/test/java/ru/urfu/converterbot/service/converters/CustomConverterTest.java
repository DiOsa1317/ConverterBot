package ru.urfu.converterbot.service.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.models.CustomQuantityType;
import ru.urfu.converterbot.service.models.fakeClasses.FakeCustomConversionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты для CustomConverter.
 */
public class CustomConverterTest {

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

        var converter = new CustomConverter(repository);

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

        var converter = new CustomConverter(repository);

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

        var converter = new CustomConverter(repository);

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
        var converter = new CustomConverter(repository);

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
