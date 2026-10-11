package ru.urfu.converterbot.service.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.TemperatureType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты для TemperatureConverter: перевод между градусами Цельсия,
 * Фаренгейта и Кельвина, перевод одинаковых шкал и обратный перевод.
 */
public class TemperatureConverterTest {

    /**
     * Экземпляр конвертера температуры, создаваемый заново перед каждым тестом.
     */
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
    @ParameterizedTest(name = "{0} {1} -> {2} = {3}")
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
    void shouldConvertTemperature(BigDecimal value, String from, String to, BigDecimal expected) {
        var request = new ConversionRequest(value, TemperatureType.valueOf(from), TemperatureType.valueOf(to));
        BigDecimal result = converter.convert(request);
        assertEquals(0, expected.compareTo(result),
                () -> "Ожидалось: " + expected + ", но получено: " + result);
    }

    /**
     * Проверяет, что перевод температуры между одинаковыми шкалами
     * возвращает исходное значение.
     */
    @Test
    @DisplayName("Перевод одинаковых единиц возвращает исходное значение")
    void shouldReturnSameValueForSameUnit() {
        BigDecimal inputValue = new BigDecimal("37.5");
        var request = new ConversionRequest(inputValue, TemperatureType.CELSIUS, TemperatureType.CELSIUS);
        BigDecimal result = converter.convert(request);
        assertEquals(0, inputValue.compareTo(result),
                "При конвертации в ту же шкалу значение должно остаться неизменным");
    }

    /**
     * Проверяет, что перевод температуры в другую шкалу и обратный перевод
     * возвращают исходное значение без потери точности.
     */
    @Test
    @DisplayName("Перевод туда и обратно возвращает исходное значение")
    void shouldReturnOriginalValueAfterRoundTrip() {
        BigDecimal originalValue = new BigDecimal("15");

        var toFahrenheitReq = new ConversionRequest(originalValue, TemperatureType.CELSIUS, TemperatureType.FAHRENHEIT);
        BigDecimal fahrenheitResult = converter.convert(toFahrenheitReq);

        var backToCelsiusReq = new ConversionRequest(fahrenheitResult, TemperatureType.FAHRENHEIT, TemperatureType.CELSIUS);
        BigDecimal celsiusResult = converter.convert(backToCelsiusReq);

        assertEquals(0, originalValue.compareTo(celsiusResult),
                () -> "После кругового преобразования значение изменилось. Было: " + originalValue + ", стало: " + celsiusResult);
    }
}