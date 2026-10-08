package ru.urfu.converterbot.service.models;

/** Единицы массы. */
public enum WeightType implements QuantityType {
    /** Килограмм*/
    KILOGRAM,
    /** Грамм, 0.001 килограмма. */
    GRAM,
    /** Тонна, 1000 килограммов. */
    TON,
    /** Английский фунт, 0.45359237 килограмма. */
    POUND
}
