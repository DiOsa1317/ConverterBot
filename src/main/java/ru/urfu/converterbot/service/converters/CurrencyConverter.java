package ru.urfu.converterbot.service.converters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.urfu.converterbot.service.converters.models.ConversionData;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.QuantityType;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 */
public class CurrencyConverter implements QuantityConverter {

    public volatile LocalDateTime lastUpdated;

    private final ConversionData currencyData;

    /**
     * Создаёт конвертер, получая данные о соотношении валют
     */
    public CurrencyConverter(CurrencyData currencyData) {
        this.currencyData = currencyData;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }
        return value.multiply(currencyData.rates().get(from)).divide(currencyData.rates().get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        var rates = new LinkedHashMap<QuantityType, BigDecimal>();
        for (CurrencyType unit : CurrencyType.values()) {
            if (unit != CurrencyType.RUB) {
                rates.put(unit, convert(BigDecimal.ONE, CurrencyType.RUB, unit));
            }
        }
        return rates;
    }

    @Override
    public QuantityType baseUnit() {
        return CurrencyType.RUB;
    }
}