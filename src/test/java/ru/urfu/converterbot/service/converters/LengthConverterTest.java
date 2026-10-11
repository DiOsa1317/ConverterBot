package ru.urfu.converterbot.service.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.LengthType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Тесты для LengthConverter. */
public class LengthConverterTest {

    /**
     * Экземпляр конвертера длины, создаваемый заново перед каждым тестом.
     */
    private LengthConverter converter;

    /**
     * Создаёт конвертер длины перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        converter = new LengthConverter();
    }

    /**
     * Проверяет перевод длины через метры как базовую единицу
     * в прямую и в обратную сторону.
     *
     * @param value    исходное значение
     * @param from     имя исходной единицы
     * @param to       имя целевой единицы
     * @param expected ожидаемый результат перевода
     */
    @ParameterizedTest(name = "{0} {1} -> {2} = {3}")
    @CsvSource({
            "1,   KILOMETER,  MILE,      0.6213711922",
            "1,   MILE,       KILOMETER, 1.609344",
            "100, METER,      CENTIMETER, 10000",
            "1,   CENTIMETER, METER,     0.01",
            "12,  INCH,       CENTIMETER, 30.48",
            "1,   INCH,       METER,     0.0254"
    })
    @DisplayName("Переводит длину через метры - базовую единицу")
    void shouldConvertLength(BigDecimal value, String from, String to, BigDecimal expected) {
        var request = new ConversionRequest(value, LengthType.valueOf(from), LengthType.valueOf(to));
        BigDecimal result = converter.convert(request);
        assertEquals(0, expected.compareTo(result),
                () -> "Ожидалось: " + expected + ", но получено: " + result);
    }
}