package ru.urfu.converterbot.service.models;

import java.util.List;


public enum TemperatureType implements QuantityType {
    CELSIUS("°C", "C", "c", "CELSIUS", "Цельсий"),
    FAHRENHEIT("°F", "F", "f", "FAHRENHEIT", "Фаренгейт"),
    KELVIN("K", "k", "KELVIN", "Кельвин", "К");

    private final List<String> codes;

    /**
     * Создаёт тип температуры с заданным набором кодов.
     *
     * @param codes коды, по которым распознаются величины
     */
    private TemperatureType(String... codes) {
        this.codes = List.of(codes);
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }

    @Override
    public BotCommand group() {
        return BotCommand.TEMPERATURE;
    }
}
