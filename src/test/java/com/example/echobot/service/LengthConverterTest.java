package com.example.echobot.service;

import com.example.echobot.service.models.LengthType;
import com.example.echobot.service.models.QuantityModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Тесты для LengthConverter. */
public class LengthConverterTest {
    private static final double DELTA = 1e-9;

    private LengthConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LengthConverter();
    }

    @ParameterizedTest(name = "{0} {1} = {2}")
    @CsvSource({
            "1,   KILOMETER,  MILE,      0.6213711922373339",
            "1,   MILE,       KILOMETER, 1.609344",
            "100, METER,      CENTIMETER, 10000",
            "1,   CENTIMETER, METER,     0.01",
            "12,  INCH,       CENTIMETER, 30.48",
            "1,   INCH,       METER,     0.0254"
    })
    @DisplayName("Переводит длину через метры - базовую единицу")
    void shouldConvertLength(double value, String from, String to, double expected) {
        var request = new QuantityModel(value, LengthType.valueOf(from), LengthType.valueOf(to));
        assertEquals(expected, converter.convert(request), DELTA);
    }
}
