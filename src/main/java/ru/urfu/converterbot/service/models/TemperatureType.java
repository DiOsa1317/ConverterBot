package ru.urfu.converterbot.service.models;

/** Единицы температуры*/
public enum TemperatureType implements QuantityType {
    /** Градус Цельсия. */
    CELSIUS,
    /** Градус Фаренгейта. */
    FAHRENHEIT,
    /** Кельвин — термодинамическая шкала, отсчёт от абсолютного нуля. */
    KELVIN
}
