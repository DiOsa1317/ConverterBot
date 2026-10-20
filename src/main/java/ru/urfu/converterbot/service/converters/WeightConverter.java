package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.WeightType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер массы.
 */
public class WeightConverter implements QuantityConverter {
    /** Сколько килограммов содержится в одной единице измерения. */
    private final Map<WeightType, BigDecimal> kilogramsInUnit = new EnumMap<>(WeightType.class);

    /**
     * Создаёт конвертер и заполняет таблицу переводов единиц в килограммы.
     */
    public WeightConverter() {
        kilogramsInUnit.put(WeightType.KILOGRAM, new BigDecimal("1.0"));
        kilogramsInUnit.put(WeightType.TON, new BigDecimal("1000.0"));
        kilogramsInUnit.put(WeightType.GRAM, new BigDecimal("0.001"));
        kilogramsInUnit.put(WeightType.POUND, new BigDecimal("0.45359237"));
    }


    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (!(from instanceof WeightType fromType)
                || !(to instanceof WeightType toType)) {
            throw new IllegalArgumentException("TemperatureConverter не может обработать запрос");
        }
        if (fromType == toType) {
            return value;
        }
        return value.multiply(kilogramsInUnit.get(from))
                .divide(kilogramsInUnit.get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        var rates = new LinkedHashMap<QuantityType, BigDecimal>();
        for (WeightType unit : WeightType.values()) {
            if (unit != WeightType.KILOGRAM) {
                rates.put(unit, convert(BigDecimal.ONE, WeightType.KILOGRAM, unit));
            }
        }
        return rates;
    }

    @Override
    public QuantityType baseUnit() {
        return WeightType.KILOGRAM;
    }
}
