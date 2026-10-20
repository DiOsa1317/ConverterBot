package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.LengthType;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер длины.
 */
public class LengthConverter implements QuantityConverter {

    /** Сколько метров содержится в одной единице измерения. */
    private final Map<LengthType, BigDecimal> metersInUnit = new EnumMap<>(LengthType.class);

   /**
    * Создаёт конвертер и заполняет таблицу переводов единиц в метры.
    */
   public LengthConverter()  {
        metersInUnit.put(LengthType.METER, new BigDecimal("1.0"));
        metersInUnit.put(LengthType.KILOMETER, new BigDecimal("1000.0"));
        metersInUnit.put(LengthType.MILE, new BigDecimal("1609.344"));
        metersInUnit.put(LengthType.CENTIMETER, new BigDecimal("0.01"));
        metersInUnit.put(LengthType.INCH, new BigDecimal("0.0254"));
    }


    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }
        return value.multiply(metersInUnit.get(from))
                .divide(metersInUnit.get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public QuantityType baseUnit() {
        return LengthType.METER;
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        var rates = new LinkedHashMap<QuantityType, BigDecimal>();
        for (LengthType unit : LengthType.values()) {
            if (unit != LengthType.METER) {
                rates.put(unit, convert(BigDecimal.ONE, LengthType.METER, unit));
            }
        }
        return rates;
    }
}
