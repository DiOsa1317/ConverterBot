package ru.urfu.converterbot.service.models;

import java.util.List;

/** Доступные единицы длины */
public enum LengthType implements QuantityType {
    METER("m", "METER", "meter", "м", "метр"),
    KILOMETER("km", "KILOMETER", "км"),
    MILE("mi", "MILE", "MI", "ми"),
    CENTIMETER("cm", "CENTIMETER", "CM", "см"),
    INCH("in", "INCH", "IN", "дюйм");

    private final List<String> codes;

    /**
     * Создаёт тип длины с заданным набором кодов.
     *
     * @param codes коды, по которым распознаются величины
     */
    LengthType(String... codes) {
        this.codes = List.of(codes);
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }
}
