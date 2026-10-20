package ru.urfu.converterbot.service.converters.models;

import ru.urfu.converterbot.service.models.LengthType;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.util.Map;

public class LengthData implements ConversionData{

    private final Map<QuantityType, BigDecimal> rates = Map.of(
            LengthType.METER, new BigDecimal("1.0"),
            LengthType.KILOMETER, new BigDecimal("1000.0"),
            LengthType.MILE, new BigDecimal("1609.344"),
            LengthType.CENTIMETER, new BigDecimal("0.01"),
            LengthType.INCH, new BigDecimal("0.0254")
    );

    @Override
    public Map<QuantityType, BigDecimal> rates() {
        return rates;
    }
}