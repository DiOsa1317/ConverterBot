package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.WeightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Тесты для WeightConverter. */
public class WeightConverterTest {

    /** Экземпляр конвертера массы, создаваемый заново перед каждым тестом. */
    private WeightConverter converter;

    /**
     * Создаёт конвертер массы перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        converter = new WeightConverter();
    }

    /**
     * Проверяет перевод массы относительно килограммов в прямую
     * и в обратную сторону.
     *
     * @param value    исходное значение
     * @param from     имя исходной единицы
     * @param to       имя целевой единицы
     * @param expected ожидаемый результат перевода
     */
    @ParameterizedTest(name = "{0} {1} = {2}")
    @CsvSource({
            "1,    KILOGRAM, POUND,    2.2046226218",
            "1,    POUND,    KILOGRAM, 0.45359237",
            "1000, GRAM,     KILOGRAM, 1",
            "1,    TON,      KILOGRAM, 1000",
            "5,    KILOGRAM, GRAM,     5000"
    })
    @DisplayName("Переводит единицы веса относительно килограммов")
    void shouldConvertWeight(BigDecimal value, String from, String to, BigDecimal expected) {
        var request = new ConversionRequest(value, WeightType.valueOf(from), WeightType.valueOf(to));
        BigDecimal result = converter.convert(request);
        assertEquals(0, expected.compareTo(result),
                "Ожидалось " + expected + ", но получили " + result);
    }
}
