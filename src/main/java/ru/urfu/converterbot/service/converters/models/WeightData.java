package ru.urfu.converterbot.service.converters.models;

import java.math.BigDecimal;
import java.util.Map;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.WeightType;

public class WeightData {

    private final Map<QuantityType, BigDecimal> rates = Map.of(
            WeightType.KILOGRAM, new BigDecimal("1.0"),
            WeightType.TON, new BigDecimal("1000.0"),
            WeightType.GRAM, new BigDecimal("0.001"),
            WeightType.POUND, new BigDecimal("0.45359237")
    );

    public Map<QuantityType, BigDecimal> rates() {
        return rates;
    }
}
