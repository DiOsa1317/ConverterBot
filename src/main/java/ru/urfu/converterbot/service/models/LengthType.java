package ru.urfu.converterbot.service.models;

/** Доступные единицы длины */
public enum LengthType implements QuantityType {
    /** Метр*/
    METER,
    /** Километр, 1000 метров. */
    KILOMETER,
    /** Сухопутная миля, 1609.344 метра. */
    MILE,
    /** Сантиметр, 0.01 метра. */
    CENTIMETER,
    /** Дюйм, 0.0254 метра. */
    INCH
}
