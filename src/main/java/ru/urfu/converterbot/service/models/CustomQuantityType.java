package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Кастомная единица измерения пользователя, обёрнутая в {@link QuantityType}
 * для участия в обычном парсинге "{число} {от} to {к}".
 */
public record CustomQuantityType(
        String name,
        long chatId
) implements QuantityType {

    @Override
    public List<String> getCodes() {
        return List.of(name);
    }

    @Override
    public BotCommand group() {
        return BotCommand.CURRENCY;
    }

    @Override
    public String toString() {
        return name;
    }
}
