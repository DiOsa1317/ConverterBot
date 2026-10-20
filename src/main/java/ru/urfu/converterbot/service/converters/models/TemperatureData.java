package ru.urfu.converterbot.service.converters.models;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.TemperatureType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class TemperatureData implements ConversionData {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal SCALE = new BigDecimal("1.8");
    private static final BigDecimal OFFSET_F = new BigDecimal("32");
    private static final BigDecimal OFFSET_K = new BigDecimal("273.15");

    private final Map<QuantityType, BigDecimal> rates = Map.of(
            TemperatureType.CELSIUS, ONE,
            TemperatureType.FAHRENHEIT, SCALE,
            TemperatureType.KELVIN, ONE
    );

    @Override
    public Map<QuantityType, BigDecimal> rates() {
        return rates;
    }

    /**
     * Возвращает смещение для перевода значения из указанной шкалы
     * в градусы Цельсия.
     *
     * @param unit исходная единица температуры
     * @return смещение, которое необходимо прибавить к значению
     */
    public BigDecimal toCelsiusOffset(QuantityType unit) {
        return switch ((TemperatureType) unit) {
            case CELSIUS -> ZERO;
            case FAHRENHEIT -> OFFSET_F.negate();
            case KELVIN -> OFFSET_K.negate();
        };
    }

    /**
     * Возвращает смещение для перевода значения из градусов Цельсия
     * в указанную температурную шкалу.
     *
     * @param unit целевая единица температуры
     * @return смещение, которое необходимо прибавить к значению
     */
    public BigDecimal fromCelsiusOffset(QuantityType unit) {
        return switch ((TemperatureType) unit) {
            case CELSIUS -> ZERO;
            case FAHRENHEIT -> OFFSET_F;
            case KELVIN -> OFFSET_K;
        };
    }

    /**
     * Возвращает коэффициент умножения при переводе значения
     * из указанной температурной шкалы в градусы Цельсия.
     *
     * @param unit исходная единица температуры
     * @return коэффициент преобразования
     */
    public BigDecimal toCelsiusRate(QuantityType unit) {
        return switch ((TemperatureType) unit) {
            case CELSIUS, KELVIN -> ONE;
            case FAHRENHEIT -> ONE.divide(SCALE, 10, RoundingMode.HALF_UP);
        };
    }

    /**
     * Возвращает коэффициент умножения при переводе значения
     * из градусов Цельсия в указанную температурную шкалу.
     *
     * @param unit целевая единица температуры
     * @return коэффициент преобразования
     */
    public BigDecimal fromCelsiusRate(QuantityType unit) {
        return switch ((TemperatureType) unit) {
            case CELSIUS, KELVIN -> ONE;
            case FAHRENHEIT -> SCALE;
        };
    }
}