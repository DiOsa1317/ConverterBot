package com.example.echobot.service.models;

/** Единицы массы, которые умеет переводить бот. */
public enum WeightType implements QuantityType {
    /** Килограмм — базовая единица массы. */
    KILOGRAM,
    /** Грамм, 0.001 килограмма. */
    GRAM,
    /** Тонна, 1000 килограммов. */
    TON,
    /** Английский фунт, 0.45359237 килограмма. */
    POUND
}
