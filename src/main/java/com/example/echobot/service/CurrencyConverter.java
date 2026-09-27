package com.example.echobot.service;

import java.util.HashMap;
import java.util.Map;

import static com.example.echobot.service.CurrencyType.*;

public class CurrencyConverter implements BotResponseProcessor {
    @Override
    public String processBotResponse(String messageText) {
        var currencyPair = clientInputParser.parse(messageText, CurrencyType.class);
        var amount = Double.parseDouble(messageText.split(" ")[0]);
        var from = currencyPair.getKey();
        var to = currencyPair.getValue();
        var convertedAmount = convertFirstCurrencyToSecond(from, to, amount);
        return String.format("%.4f %s = %.4f $s", amount, from, convertedAmount, to);
    }

    private final ClientInputParser clientInputParser;

    private static final Map<CurrencyType, Double> RATES_TO_RUB = new HashMap<>();

    static {
        RATES_TO_RUB.put(USD, 84.3414);
        RATES_TO_RUB.put(EUR, 95.8709);
        RATES_TO_RUB.put(CNY, 12.5355);
        RATES_TO_RUB.put(KZT, 1.91);
    }

    public CurrencyConverter() {
        clientInputParser = new ClientInputParser();
    }

    public String getActualCurrency() {
        return String.format("""
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

    private Double convertFirstCurrencyToSecond(CurrencyType from, CurrencyType to, Double amount) {
        if (from == to)
            return amount;
        var amountInRub = from == RUB ? amount : amount * RATES_TO_RUB.get(from);
        return to == RUB? amountInRub : amountInRub / RATES_TO_RUB.get(to);
    }

}
