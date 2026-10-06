package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.TemperatureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты для TemperatureConverter: перевод между градусами Цельсия,
 * Фаренгейта и Кельвина, перевод одинаковых шкал и обратный перевод.
 */
public class TemperatureConverterTest {

    /** Допустимая погрешность сравнения вещественных чисел. */
    private static final double DELTA = 1e-9;

    /** Экземпляр конвертера температуры, создаваемый заново перед каждым тестом. */
    private TemperatureConverter converter;

    /**
     * Создаёт конвертер температуры перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        converter = new TemperatureConverter();
    }

    /**
     * Проверяет перевод температуры между всеми парами шкал группы,
     * включая отрицательные значения и абсолютный ноль.
     *
     * @param value    исходное значение температуры
     * @param from     имя исходной шкалы
     * @param to       имя целевой шкалы
     * @param expected ожидаемый результат перевода
     */
    @ParameterizedTest(name = "{0} {1} = {2}")
    @CsvSource({
            "0, CELSIUS, FAHRENHEIT, 32",
            "20, CELSIUS, FAHRENHEIT, 68",
            "-40, CELSIUS, FAHRENHEIT, -40",
            "100, CELSIUS, FAHRENHEIT, 212",
            "212, FAHRENHEIT, CELSIUS, 100",
            "32, FAHRENHEIT, CELSIUS, 0",
            "0, CELSIUS, KELVIN, 273.15",
            "0, KELVIN, CELSIUS, -273.15",
            "300, KELVIN, CELSIUS, 26.85",
            "0, KELVIN, FAHRENHEIT, -459.67"
    })
    @DisplayName("Переводит температуру между градусами Цельсия, Фаренгейта и Кельвина")
    void shouldConvertTemperature(double value, String from, String to, double expected) {
        var request = new QuantityModel(value, TemperatureType.valueOf(from), TemperatureType.valueOf(to));
        assertEquals(expected, converter.convert(request), DELTA);
    }

    /**
     * Проверяет, что перевод температуры между одинаковыми шкалами
     * возвращает исходное значение.
     */
    @Test
    @DisplayName("Перевод одинаковых единиц возвращает исходное значение")
    void shouldReturnSameValueForSameUnit() {
        var request = new QuantityModel(37.5, TemperatureType.CELSIUS, TemperatureType.CELSIUS);
        assertEquals(37.5, converter.convert(request), DELTA);
    }

    /**
     * Проверяет, что перевод из °C в °F и обратно возвращает исходное
     * значение — аффинное преобразование не теряет точность.
     */
    @Test
    @DisplayName("Перевод туда и обратно возвращает исходное значение")
    void shouldReturnOriginalValueAfterRoundTrip() {
        var toFahrenheit = new QuantityModel(15.0, TemperatureType.CELSIUS, TemperatureType.FAHRENHEIT);
        var converted = converter.convert(toFahrenheit);
        var back = new QuantityModel(converted, TemperatureType.FAHRENHEIT, TemperatureType.CELSIUS);
        assertEquals(15.0, converter.convert(back), DELTA);
    }
}
