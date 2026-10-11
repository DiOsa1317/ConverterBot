package ru.urfu.converterbot.service.models;

import java.util.List;

/** Доступные единицы длины */
public enum LengthType implements QuantityType {
    METER("m", "meter", "м", "метр"),
    KILOMETER,
    MILE,
    CENTIMETER,
    INCH;

    private final List<String> codes;

    LengthType(String... codes) {
        this.codes = List.of(codes);
    }
}
