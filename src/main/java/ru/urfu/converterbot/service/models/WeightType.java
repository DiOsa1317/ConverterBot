package ru.urfu.converterbot.service.models;

import java.util.List;


public enum WeightType implements QuantityType {
    KILOGRAM("kg", "KILOGRAM", "KG", "килограмм", "кг"),
    GRAM("g", "GRAM", "G", "грамм", "г"),
    TON("t", "TON", "тонна", "т"),
    POUND("lb", "POUND", "фунт");

    /** Допустимые коды веса: первый — канонический, остальные — синонимы. */
    private final List<String> codes;

    /**
     * Создаёт тип веса с заданным набором кодов.
     *
     * @param codes коды, по которым распознаются величины
     */
    WeightType(String... codes) {
        this.codes = List.of(codes);
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }

    @Override
    public BotCommand group() {
        return BotCommand.WEIGHT;
    }
}
