package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;

public record CustomConversion(
        String fromUnit,
        String toUnit,
        BigDecimal rate,
        long chatId
) {}
