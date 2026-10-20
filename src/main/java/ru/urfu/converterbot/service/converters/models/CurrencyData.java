package ru.urfu.converterbot.service.converters.models;

import java.math.BigDecimal;
import java.util.Map;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.CurrencyType;

public class CurrencyData implements ConversionData{

    private final Map<QuantityType, BigDecimal> rates = Map.of(
            CurrencyType.RUB, new BigDecimal("1.0"),
            CurrencyType.USD, new BigDecimal("84.3414"),
            CurrencyType.EUR, new BigDecimal("95.8709"),
            CurrencyType.CNY, new BigDecimal("12.5355"),
            CurrencyType.KZT, new BigDecimal("1.91")
    );

    @Override
    public Map<QuantityType, BigDecimal> rates() {
        return rates;
    }
    
}
