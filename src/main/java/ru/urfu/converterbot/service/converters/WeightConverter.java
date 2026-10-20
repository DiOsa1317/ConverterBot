package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.converters.models.ConversionData;
import ru.urfu.converterbot.service.converters.models.WeightData;
import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.WeightType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер массы.
 */
public class WeightConverter implements QuantityConverter {

    private final ConversionData weightData;

    /**
     * Создаёт конвертер, получая данные о соотношении весов
     */
    public WeightConverter(WeightData weightData) {
        this.weightData = weightData;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }

        return value.multiply(weightData.rates().get(from))
                .divide(weightData.rates().get(to), 10, RoundingMode.HALF_UP);
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
