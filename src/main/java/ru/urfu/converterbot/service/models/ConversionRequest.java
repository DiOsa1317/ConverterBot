package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;

/**
 * Формат разбора запроса на конвертацию: число и единицы измерения одного типа,
 * между которыми нужно выполнить перевод.
 *
 * @param value числовое значение, которое нужно перевести
 * @param from единица измерения, из которой переводим
 * @param to единица измерения, в которую переводим
 */
public record ConversionRequest(
    BigDecimal value, 
    QuantityType from, 
    QuantityType to) {
    /**
     * Компактный конструктор: проверяет, что обе единицы измерения принадлежат
     * одной и той же группе величин (оба — валюты, оба — величины длины, и т.д.).
     *
     * @throws InvalidUserInputException если единицы принадлежат разным группам
     */
    public ConversionRequest {
        if (from.getClass() != to.getClass()) {
            throw new InvalidUserInputException(
                    from + " to " + to,
                    "Нельзя перевести " + from + " в " + to + ": это разные группы величин."
            );
        }
    }
}

