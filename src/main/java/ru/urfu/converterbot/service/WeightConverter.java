package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.WeightType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Конвертер массы. Все переводы идут через килограмм как базовую единицу,
 * поэтому перевод в обе стороны симметричен и не требует таблицы пар.
 */
public class WeightConverter implements QuantityConverter {
    /** Сколько килограммов содержится в одной единице измерения. */
    private final Map<WeightType, BigDecimal> kilogramsInUnit = new EnumMap<>(WeightType.class);

    /**
     * Создаёт конвертер и заполняет таблицу переводов единиц в килограммы.
     */
    public WeightConverter() {
        kilogramsInUnit.put(WeightType.KILOGRAM, new BigDecimal("1.0"));
        kilogramsInUnit.put(WeightType.TON, new BigDecimal("1000.0"));
        kilogramsInUnit.put(WeightType.GRAM, new BigDecimal("0.001"));
        kilogramsInUnit.put(WeightType.POUND, new BigDecimal("0.45359237"));
    }

    @Override
    public String title() {
        return "Вес";
    }

    @Override
    public String command() {
        return "/weight";
    }

    @Override
    public Class<WeightType> quantityType() {
        return WeightType.class;
    }

    @Override
    public BigDecimal convert(ConversionRequest request) {
        var from = (WeightType) request.from();
        var to = (WeightType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value().multiply(kilogramsInUnit.get(from))
                .divide(kilogramsInUnit.get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public String getUnitsDescription() {
        return String.format(Locale.US, """
                Величины весов относительно килограммов:
                Грамм = %f кг;
                Фунт = %f кг;
                Тонна = %f кг;
                """,
                kilogramsInUnit.get(WeightType.GRAM),
                kilogramsInUnit.get(WeightType.POUND),
                kilogramsInUnit.get(WeightType.TON));
    }
}
