package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.CustomQuantityType;
import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.CustomConversionRepository;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 * Поддерживает прямые конверсии между кастомными единицами пользователя.
 */
public class CurrencyConverter implements QuantityConverter {

    private final CurrencyData currencyData;
    private final CustomConversionRepository conversionRepository;

    /**
     * Создаёт конвертер, получая данные о соотношении валют
     */
    public CurrencyConverter(CurrencyData currencyData) {
        this.currencyData = currencyData;
        this.conversionRepository = null;
    }

    public CurrencyConverter(CurrencyData currencyData,
                             CustomConversionRepository conversionRepository) {
        this.currencyData = currencyData;
        this.conversionRepository = conversionRepository;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }

        // Если обе единицы кастомные — ищем прямую конверсию в БД
        if (from instanceof CustomQuantityType && to instanceof CustomQuantityType) {
            var customFrom = (CustomQuantityType) from;
            var customTo = (CustomQuantityType) to;
            long chatId = customFrom.chatId();
            try {
                var conv = conversionRepository.findByChatidFromAndAndTo(
                        chatId, customFrom.name(), customTo.name());
                if (conv.isPresent()) {
                    return value.multiply(conv.get().rate())
                            .setScale(10, RoundingMode.HALF_UP);
                }
                // Пробуем обратное ребро
                var reverse = conversionRepository.findByChatidFromAndAndTo(
                        chatId, customTo.name(), customFrom.name());
                if (reverse.isPresent()) {
                    return value.divide(reverse.get().rate(), 10, RoundingMode.HALF_UP);
                }
                throw new InvalidUserInputException(
                        customFrom.name() + " to " + customTo.name(),
                        "Нет прямого курса между " + customFrom.name() + " и " + customTo.name());
            } catch (SQLException e) {
                throw new InvalidUserInputException(
                        customFrom.name() + " to " + customTo.name(),
                        "Ошибка базы данных при конвертации.");
            }
        }

        // Стандартная логика через матрицу курсов CurrencyData
        return value.multiply(currencyData.rates().get(from).get(to))
                .setScale(10, RoundingMode.HALF_UP);
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

    /**
     * Возвращает время последнего обновления курсов.
     *
     * @return время последнего обновления или {@code null}, если обновления ещё не было
     */
    public LocalDateTime lastUpdated() {
        return currencyData.lastUpdated();
    }

    @Override
    public QuantityType baseUnit() {
        return CurrencyType.RUB;
    }
}
