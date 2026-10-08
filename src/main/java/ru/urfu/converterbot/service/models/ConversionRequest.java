package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;

/**
 * Формат разбора запроса на конвертацию: число и единицы измерения одного типа,
 * между которыми нужно выполнить перевод.
 *
 * @param value числовое значение, которое нужно перевести
 * @param from единица измерения, из которой переводим
 * @param to единица измерения, в которую переводим
 */
public record ConversionRequest(BigDecimal value, QuantityType from, QuantityType to) {
}

