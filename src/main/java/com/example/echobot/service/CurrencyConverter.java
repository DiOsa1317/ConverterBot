package com.example.echobot.service;

import static com.example.echobot.service.models.CurrencyType.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.QuantityModel;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 * Использует фиксированные курсы относительно рубля (RUB) как базовой валюты.
 */
public class CurrencyConverter {

    /** Карта курсов валют относительно рубля (сколько рублей стоит 1 единица валюты) */
    private static final Map<CurrencyType, Double> RATES_TO_RUB = new HashMap<>();

    static {
        RATES_TO_RUB.put(USD, 84.3414);
        RATES_TO_RUB.put(EUR, 95.8709);
        RATES_TO_RUB.put(CNY, 12.5355);
        RATES_TO_RUB.put(KZT, 1.91);
    }

    /**
     * Возвращает строку с актуальными курсами валют.
     *
     * @return информация о курсах всех доступных валют относительно рубля
     */
    public String getActualCurrency() {
        return String.format(Locale.US, """
                Доступны переводы по таким курсам:
                Рубли к долларам - %.4f;
                Рубли к евро - %.4f;
                Рубли к юаням - %.4f;
                Рубли к тенге - %.4f;
                """,
                RATES_TO_RUB.get(CurrencyType.USD),
                RATES_TO_RUB.get(CurrencyType.EUR),
                RATES_TO_RUB.get(CurrencyType.CNY),
                RATES_TO_RUB.get(CurrencyType.KZT));
    }

    /**
     * Конвертирует сумму из одной валюты в другую через рубль.
     *
     * @param from   исходная валюта
     * @param to     целевая валюта
     * @param amount сумма для конвертации
     * @return конвертированная сумма
     */
    public Double convertFirstCurrencyToSecond(QuantityModel quantityModel) {
        if (quantityModel.from() == quantityModel.to())
            return quantityModel.value();
        var amountInRub = quantityModel.from() == RUB ? quantityModel.value() 
        : quantityModel.value() * RATES_TO_RUB.get(quantityModel.from());
        return quantityModel.to() == RUB? amountInRub : amountInRub / RATES_TO_RUB.get(quantityModel.to());
    }
}