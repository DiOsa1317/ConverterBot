package ru.urfu.converterbot.service.models;

import java.util.List;


public enum CurrencyType implements QuantityType {
    RUB("RUB", "RUBLE", "руб"),
    EUR("EUR", "EURO", "евро"),
    USD("USD", "DOLLAR", "доллар"),
    CNY("CNY", "YUAN", "юань"),
    KZT("KZT", "TENGE", "тенге");

    private final List<String> codes;

    /**
     * Создаёт тип валюты с заданным набором кодов.
     *
     * @param codes коды, по которым распознаются величины
     */
    private CurrencyType(String... codes) {
        this.codes = List.of(codes);
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }

    @Override
    public BotCommand group() {
        return BotCommand.CURRENCY;
    }
}
