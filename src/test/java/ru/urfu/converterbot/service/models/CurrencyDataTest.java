package ru.urfu.converterbot.service.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.service.converters.models.CurrencyData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyDataTest {
    private CurrencyData currencyData;

    @BeforeEach
    void setUp() {
        currencyData = new CurrencyData();
    }

    @Test
    @DisplayName("До обновления время обновления не задано")
    void shouldHaveNoUpdateTimeInitially() {
        assertNull(currencyData.lastUpdated());
    }

    @Test
    @DisplayName("Обновляет курсы и время обновления")
    void shouldUpdateRatesAndTime() {
        var updatedAt = LocalDateTime.of(2026, 10, 23, 14, 5);
        currencyData.update(completeRates(new BigDecimal("90.5")), updatedAt);

        assertEquals(updatedAt, currencyData.lastUpdated());
        assertEquals(0, new BigDecimal("90.5")
                .compareTo(currencyData.rates().get(CurrencyType.USD)));
    }

    @Test
    @DisplayName("Заменяет ранее загруженные курсы")
    void shouldReplacePreviousDates() {
        var firstTime = LocalDateTime.of(2026, 10, 23, 14, 5);
        var secondTime = firstTime.plusHours(6);

        currencyData.update(completeRates(new BigDecimal("90.5")), firstTime);
        currencyData.update(completeRates(new BigDecimal("91.5")), secondTime);

        assertEquals(secondTime, currencyData.lastUpdated());
        assertEquals(0, new BigDecimal("91.5")
                .compareTo(currencyData.rates().get(CurrencyType.USD)));
    }

    @Test
    @DisplayName("Не принимает неполный набор курсов")
    void shouldRejectIncompleteRates() {
        var updatedAt = LocalDateTime.of(2026, 10, 23, 14, 5);
        Map<CurrencyType, BigDecimal> incomplete = new EnumMap<>(CurrencyType.class);
        incomplete.put(CurrencyType.RUB, BigDecimal.ONE);

        var previousRates = currencyData.rates();
        assertThrows(IllegalArgumentException.class,
                () -> currencyData.update(incomplete, updatedAt));
        assertSame(previousRates, currencyData.rates());
        assertNull(currencyData.lastUpdated());
    }

    @Test
    @DisplayName("Не позволяет изменять курсы через rates()")
    void shouldExposeImmutableRates() {
        currencyData.update(completeRates(new BigDecimal("90.5")),
                LocalDateTime.of(2026, 10, 23, 14, 5));

        assertThrows(UnsupportedOperationException.class,
                () -> currencyData.rates().put(CurrencyType.USD, BigDecimal.ONE));
    }

    private static Map<CurrencyType, BigDecimal> completeRates(BigDecimal usd) {
        var rates = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
        rates.put(CurrencyType.RUB, BigDecimal.ONE);
        rates.put(CurrencyType.USD, usd);
        rates.put(CurrencyType.EUR, new BigDecimal("95.0"));
        rates.put(CurrencyType.KZT, new BigDecimal("1.9"));
        rates.put(CurrencyType.CNY, new BigDecimal("12.5"));
        return rates;
    }
}
