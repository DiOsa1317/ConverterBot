package ru.urfu.converterbot.service.models;

import java.math.BigDecimal;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;

/**
 * Формат разбора запроса на конвертацию: число и единицы измерения одного типа,
 * между которыми нужно выполнить перевод.
 */
public class ConversionRequest {
    /** Числовое значение, которое нужно перевести. */
    private final BigDecimal value;

    /** Единица измерения, из которой переводим. */
    private final QuantityType from;

    /** Единица измерения, в которую переводим. */
    private final QuantityType to;

    /**
     * Создаёт запрос, проверяя, что единицы принадлежат одной группе величин.
     *
     * @param value числовое значение
     * @param from исходная единица
     * @param to целевая единица
     * @throws InvalidUserInputException если единицы принадлежат разным группам
     */
    public ConversionRequest(BigDecimal value, QuantityType from, QuantityType to)
            throws InvalidUserInputException {
        if (from.getClass() != to.getClass()) {
            throw new InvalidUserInputException(from + " to " + to,
                    "Нельзя перевести " + from + " в " + to + ": это разные группы величин.");
        }
        this.value = value;
        this.from = from;
        this.to = to;
    }

    /** @return числовое значение */
    public BigDecimal value() { return value; }

    /** @return исходная единица */
    public QuantityType from() { return from; }

    /** @return целевая единица */
    public QuantityType to() { return to; }

    @Override
    public String toString() {
        return value + " " + from + " to " + to;
    }
}

