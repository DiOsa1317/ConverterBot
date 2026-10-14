package ru.urfu.converterbot.service.models;

import java.util.List;


public enum TemperatureType implements QuantityType {
    CELSIUS("°C", "CELSIUS", "Цельсий"),
    FAHRENHEIT("°F", "FAHRENHEIT", "Фаренгейт"),
    KELVIN("K", "KELVIN", "Кельвин", "К");

    private final List<String> codes;

    /**
     * Создаёт тип температуры с заданным набором кодов.
     *
     * @param codes коды, по которым распознаются величины
     */
    TemperatureType(String... codes) {
        this.codes = List.of(codes);
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }
}
