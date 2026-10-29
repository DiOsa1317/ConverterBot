package ru.urfu.converterbot.keyboard.models;

public record ChatContext(
        State state,
        String conversionType,
        String number,
        String fromUnit,
        String toUnit
) {}
