package com.example.echobot.service;

import java.util.HashMap;
import java.util.Map;

import static com.example.echobot.service.CurrencyType.*;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 * Использует фиксированные курсы относительно рубля (RUB) как базовой валюты.
 */
public class CurrencyConverter implements BotResponseProcessor {

    private final ClientInputParser clientInputParser;

    /** Карта курсов валют относительно рубля (сколько рублей стоит 1 единица валюты) */
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

    /**
     * Обрабатывает сообщение пользователя и выполняет конвертацию валют.
     *
     * @param messageText входное сообщение формата "{сумма} {валюта1} to {валюта2}",
     *                    например "100 USD to EUR"
     * @return строка с результатом конвертации в формате "{сумма} {валюта1} = {результат} {валюта2}"
     */
    @Override
    public String processBotResponse(String messageText) {
        var currencyPair = clientInputParser.parse(messageText, CurrencyType.class);
        var amount = Double.parseDouble(messageText.split(" ")[0]);
        var from = currencyPair.getKey();
        var to = currencyPair.getValue();
        var convertedAmount = convertFirstCurrencyToSecond(from, to, amount);
        return String.format("%.4f %s = %.4f %s", amount, from, convertedAmount, to);
    }

    /**
     * Возвращает строку с актуальными курсами валют.
     *
     * @return информация о курсах всех доступных валют относительно рубля
     */
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

    /**
     * Конвертирует сумму из одной валюты в другую через рубль.
     *
     * @param from   исходная валюта
     * @param to     целевая валюта
     * @param amount сумма для конвертации
     * @return конвертированная сумма
     */
    private Double convertFirstCurrencyToSecond(CurrencyType from, CurrencyType to, Double amount) {
        if (from == to)
            return amount;
        var amountInRub = from == RUB ? amount : amount * RATES_TO_RUB.get(from);
        return to == RUB? amountInRub : amountInRub / RATES_TO_RUB.get(to);
    }

}