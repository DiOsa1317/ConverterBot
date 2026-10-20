package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.TemperatureType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер температуры.
 */
public class TemperatureConverter implements QuantityConverter {

    /** Сколько градусов Цельсия в одном градусе Фаренгейта. */
    private static final BigDecimal FAHRENHEIT_IN_CELSIUS = new BigDecimal("1.8");

    /** Смещение шкалы Фаренгейта относительно шкалы Цельсия. */
    private static final BigDecimal FAHRENHEIT_OFFSET = new BigDecimal("32.0");

    /** Абсолютный ноль в градусах Цельсия — база шкалы Кельвина. */
    private static final BigDecimal ABSOLUTE_ZERO_IN_CELSIUS = new BigDecimal("273.15");

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (!(from instanceof TemperatureType fromType)
                || !(to instanceof TemperatureType toType)) {
            throw new IllegalArgumentException("TemperatureConverter не может обработать запрос");
        }
        if (fromType == toType) {
            return value;
        }
        return fromCelsius(toCelsius(value, fromType), toType);
    }

    /**
     * Приводит исходное значение к градусам Цельсия.
     *
     * @param value исходное значение температуры
     * @param unit  шкала, в которой задано исходное значение
     * @return то же значение в градусах Цельсия
     */
    private BigDecimal toCelsius(BigDecimal value, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS ->  value;
            case FAHRENHEIT -> value.subtract(FAHRENHEIT_OFFSET).divide(FAHRENHEIT_IN_CELSIUS, 10, RoundingMode.HALF_UP);
            case KELVIN -> value.subtract(ABSOLUTE_ZERO_IN_CELSIUS);
        };
    }

    /**
     * Выражает значение в градусах Цельсия в целевой шкале.
     *
     * @param celsius значение в градусах Цельсия
     * @param unit    целевая шкала
     * @return значение в целевой шкале
     */
    private BigDecimal fromCelsius(BigDecimal celsius, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS -> celsius;
            case FAHRENHEIT -> celsius.multiply(FAHRENHEIT_IN_CELSIUS).add(FAHRENHEIT_OFFSET);
            case KELVIN -> celsius.add(ABSOLUTE_ZERO_IN_CELSIUS);
        };
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        var rates = new LinkedHashMap<QuantityType, BigDecimal>();
        for (TemperatureType unit : TemperatureType.values()) {
            if (unit != TemperatureType.CELSIUS) {
                rates.put(unit, convert(BigDecimal.ONE, TemperatureType.CELSIUS, unit));
            }
        }
        return rates;
    }

    @Override
    public QuantityType baseUnit() {
        return TemperatureType.CELSIUS;
    }
}
