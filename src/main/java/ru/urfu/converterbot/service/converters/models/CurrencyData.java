package ru.urfu.converterbot.service.converters.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import ru.urfu.converterbot.service.models.CurrencyType;

/**
 * Хранит курсы валют в виде матрицы «каждый к каждому».
 */
public class CurrencyData {

    /**
     * Актуальные курсы: rates.get(from).get(to) — сколько валюты to в одной единице валюты from.
     */
    private volatile Map<CurrencyType, Map<CurrencyType, BigDecimal>> rates = buildInitialMatrix();
    private volatile LocalDateTime lastUpdated;

    private static Map<CurrencyType, Map<CurrencyType, BigDecimal>> buildInitialMatrix() {
        var rubRates = Map.of(
                CurrencyType.RUB, new BigDecimal("1.0"),
                CurrencyType.USD, new BigDecimal("84.3414"),
                CurrencyType.EUR, new BigDecimal("95.8709"),
                CurrencyType.CNY, new BigDecimal("12.5355"),
                CurrencyType.KZT, new BigDecimal("1.91")
        );
        var matrix = new EnumMap<CurrencyType, Map<CurrencyType, BigDecimal>>(CurrencyType.class);
        for (var from : CurrencyType.values()) {
            var row = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
            for (var to : CurrencyType.values()) {
                row.put(to, rubRates.get(from).divide(rubRates.get(to), 20, RoundingMode.HALF_UP));
            }
            matrix.put(from, row);
        }
        return matrix;
    }

    public Map<CurrencyType, Map<CurrencyType, BigDecimal>> rates() {
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
     * @param newRates  новая матрица курсов валют
     * @param updatedAt время обновления
     * @throws IllegalArgumentException если набор курсов неполный
     */
    public void update(Map<CurrencyType, Map<CurrencyType, BigDecimal>> newRates, LocalDateTime updatedAt) {
        var allTypes = EnumSet.allOf(CurrencyType.class);
        if (!newRates.keySet().equals(allTypes)) {
            throw new IllegalArgumentException("Неполный набор валют в матрице курсов: " + newRates.keySet());
        }
        var immutable = new EnumMap<CurrencyType, Map<CurrencyType, BigDecimal>>(CurrencyType.class);
        for (var entry : newRates.entrySet()) {
            if (!entry.getValue().keySet().equals(allTypes)) {
                throw new IllegalArgumentException("Неполная строка для валюты: " + entry.getKey());
            }
            immutable.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        rates = Map.copyOf(immutable);
        lastUpdated = updatedAt;
    }
}
