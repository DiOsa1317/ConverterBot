package ru.urfu.converterbot.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.ConversionRequest;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 * Использует фиксированные курсы относительно рубля (RUB) как базовой валюты.
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
    public String title() {
        return "Валюты";
    }

    @Override
    public String command() {
        return "/currency";
    }

    @Override
    public Class<CurrencyType> quantityType() {
        return CurrencyType.class;
    }

    @Override
    public BigDecimal convert(ConversionRequest request) {
        var from = (CurrencyType) request.from();
        var to = (CurrencyType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value().multiply(ratesToRub.get(from)).divide(ratesToRub.get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public String getUnitsDescription() {
        return String.format(Locale.US, """
                Доступны переводы по таким курсам:
                Рубли к долларам - %.4f;
                Рубли к евро - %.4f;
                Рубли к юаням - %.4f;
                Рубли к тенге - %.4f;
                """,
                ratesToRub.get(CurrencyType.USD),
                ratesToRub.get(CurrencyType.EUR),
                ratesToRub.get(CurrencyType.CNY),
                ratesToRub.get(CurrencyType.KZT));
    }
}