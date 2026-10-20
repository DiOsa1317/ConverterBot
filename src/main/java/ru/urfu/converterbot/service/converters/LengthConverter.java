package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.converters.models.ConversionData;
import ru.urfu.converterbot.service.converters.models.LengthData;
import ru.urfu.converterbot.service.models.LengthType;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер длины.
 */
public class LengthConverter implements QuantityConverter {

    private final ConversionData lengthData;

    /**
     * Создаёт конвертер, получая данные о соотношении длин
     */
    public LengthConverter(LengthData lengthData)  {
        this.lengthData = lengthData;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }
        return value.multiply(lengthData.rates().get(from))
                .divide(lengthData.rates().get(to), 10, RoundingMode.HALF_UP);
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

    @Override
    public QuantityType baseUnit() {
        return LengthType.METER;
    }
}
