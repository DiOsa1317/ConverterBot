package ru.urfu.converterbot.service.converters;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.QuantityType;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 */
public class CurrencyConverter implements QuantityConverter {

    /** Карта курсов валют относительно рубля */
    private final Map<CurrencyType, BigDecimal> ratesToRub = new EnumMap<>(CurrencyType.class);

    /**
     * Создаёт конвертер и заполняет таблицу курсов валют к рублю.
     */
    public CurrencyConverter() {
        ratesToRub.put(CurrencyType.RUB, new BigDecimal("1.0"));
        ratesToRub.put(CurrencyType.USD, new BigDecimal("84.3414"));
        ratesToRub.put(CurrencyType.EUR, new BigDecimal("95.8709"));
        ratesToRub.put(CurrencyType.CNY, new BigDecimal("12.5355"));
        ratesToRub.put(CurrencyType.KZT, new BigDecimal("1.91"));
    }


    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }
        return value.multiply(ratesToRub.get(from)).divide(ratesToRub.get(to), 10, RoundingMode.HALF_UP);
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