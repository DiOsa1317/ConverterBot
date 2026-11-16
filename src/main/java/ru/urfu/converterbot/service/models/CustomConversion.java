package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;

/**Данные о кастомном переводе*/
public record CustomConversion(
        String fromUnit,
        String toUnit,
        BigDecimal rate,
        long chatId
) {}
