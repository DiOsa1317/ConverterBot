package ru.urfu.converterbot.service.converters.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;

import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.CurrencyType;

/**
 * Хранит курсы валют.
 */
public class CurrencyData implements ConversionData {

    /**
     * Курсы по умолчанию
     */
    private static final Map<QuantityType, BigDecimal> DEFAULT_RATES = Map.of(
            CurrencyType.RUB, new BigDecimal("1.0"),
            CurrencyType.USD, new BigDecimal("84.3414"),
            CurrencyType.EUR, new BigDecimal("95.8709"),
            CurrencyType.CNY, new BigDecimal("12.5355"),
            CurrencyType.KZT, new BigDecimal("1.91")
    );

    /**
     * Актуальные курсы
     */
    private volatile Map<QuantityType, BigDecimal> rates = DEFAULT_RATES;

    /**
     * Время последнего успешного обновления
     */
    private volatile LocalDateTime lastUpdated;

    @Override
    public Map<QuantityType, BigDecimal> rates() {
        return rates;
    }

    /**
     * Возвращает время последнего обновления курсов.
     *
     * @return время последнего обновления или {@code null}, если обновления ещё не было
     */
    public LocalDateTime lastUpdated() {
        return lastUpdated;
    }

    /**
     * Обновляет курсы валют.
     *
     * @param newRates  новые курсы валют
     * @param updatedAt время обновления
     * @throws IllegalArgumentException если набор курсов неполный
     */
    public void update(Map<CurrencyType, BigDecimal> newRates, LocalDateTime updatedAt) {

        if (!newRates.keySet().containsAll(EnumSet.allOf(CurrencyType.class))) {
            throw new IllegalArgumentException("Неполный набор курсов валют: " + newRates.keySet());
        }
        rates = Map.copyOf(newRates);
        lastUpdated = updatedAt;
    }
}
