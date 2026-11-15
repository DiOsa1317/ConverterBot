package ru.urfu.converterbot.service.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.service.converters.models.CurrencyData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты контракта хранилища курсов валют {@link CurrencyData}.
 */
public class CurrencyDataTest {
    /** Хранилище курсов валют, используемое в тестах. */
    private CurrencyData currencyData;

    @BeforeEach
    void setUp() {
        currencyData = new CurrencyData();
    }

    /**
     * Проверяет начальное состояние: время последнего обновления отсутствует.
     */
    @Test
    @DisplayName("До обновления время обновления не задано")
    void shouldHaveNoUpdateTimeInitially() {
        assertNull(currencyData.lastUpdated());
    }

    /**
     * Проверяет, что успешное обновление записывает новые курсы и актуальную метку времени.
     */
    @Test
    @DisplayName("Обновляет курсы и время обновления")
    void shouldUpdateRatesAndTime() {
        var updatedAt = LocalDateTime.of(2026, 10, 23, 14, 5);
        currencyData.update(completeRates(new BigDecimal("90.5")), updatedAt);

        assertEquals(updatedAt, currencyData.lastUpdated());
        assertEquals(0, new BigDecimal("90.5")
                .compareTo(currencyData.rates().get(CurrencyType.USD).get(CurrencyType.RUB)));
    }

    /**
     * Проверяет атомарность обновления: при некорректном входном наборе
     * состояние хранилища остаётся неизменным.
     */
    @Test
    @DisplayName("Не принимает неполный набор курсов")
    void shouldRejectIncompleteRates() {
        var updatedAt = LocalDateTime.of(2026, 10, 23, 14, 5);
        Map<CurrencyType, Map<CurrencyType, BigDecimal>> incomplete = new EnumMap<>(CurrencyType.class);
        incomplete.put(CurrencyType.RUB, Map.of(CurrencyType.RUB, BigDecimal.ONE));

        var previousRates = currencyData.rates();
        assertThrows(IllegalArgumentException.class,
                () -> currencyData.update(incomplete, updatedAt));
        assertSame(previousRates, currencyData.rates());
        assertNull(currencyData.lastUpdated());
    }

    /**
     * Проверяет, что возвращаемая коллекция курсов защищена от внешней модификации.
     */
    @Test
    @DisplayName("Не позволяет изменять курсы через rates()")
    void shouldExposeImmutableRates() {
        currencyData.update(completeRates(new BigDecimal("90.5")),
                LocalDateTime.of(2026, 10, 23, 14, 5));

        assertThrows(UnsupportedOperationException.class,
                () -> currencyData.rates().put(CurrencyType.USD, Map.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> currencyData.rates().get(CurrencyType.USD).put(CurrencyType.RUB, BigDecimal.ONE));
    }

    /**
     * Формирует валидную полную матрицу курсов для тестовых сценариев.
     *
     * @param usd курс доллара США к рублю
     * @return матрица со всеми валютами из {@link CurrencyType}
     */
    private static Map<CurrencyType, Map<CurrencyType, BigDecimal>> completeRates(BigDecimal usd) {
        var allTypes = EnumSet.allOf(CurrencyType.class);
        var matrix = new EnumMap<CurrencyType, Map<CurrencyType, BigDecimal>>(CurrencyType.class);
        for (var from : allTypes) {
            var row = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
            for (var to : allTypes) {
                if (from == to) {
                    row.put(to, BigDecimal.ONE);
                } else if (from == CurrencyType.RUB) {
                    row.put(to, usd.equals(BigDecimal.ONE) ? BigDecimal.ONE : BigDecimal.ZERO);
                } else if (to == CurrencyType.RUB && from == CurrencyType.USD) {
                    row.put(to, usd);
                } else {
                    row.put(to, BigDecimal.ZERO);
                }
            }
            matrix.put(from, row);
        }
        return matrix;
    }
}
