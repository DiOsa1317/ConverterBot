package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.WeightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Тесты для WeightConverter. */
public class WeightConverterTest {

    /** Допустимая погрешность сравнения вещественных чисел. */
    private static final double DELTA = 1e-9;

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
            "1,    KILOGRAM, POUND,    2.2046226218487757",
            "1,    POUND,    KILOGRAM, 0.45359237",
            "1000, GRAM,     KILOGRAM, 1",
            "1,    TON,      KILOGRAM, 1000",
            "5,    KILOGRAM, GRAM,     5000"
    })
    @DisplayName("Переводит единицы веса относительно килограммов")
    void shouldConvertWeight(double value, String from, String to, double expected) {
        var request = new QuantityModel(value, WeightType.valueOf(from), WeightType.valueOf(to));
        assertEquals(expected, converter.convert(request), DELTA);
    }
}
