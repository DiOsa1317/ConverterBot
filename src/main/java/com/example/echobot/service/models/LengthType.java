package com.example.echobot.service.models;

/** Единицы длины, которые умеет переводить бот. */
public enum LengthType implements QuantityType {
    /** Метр — базовая единица длины. */
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
