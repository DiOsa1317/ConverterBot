package com.example.echobot.service.models;

/** Единицы температуры, которые умеет переводить бот. */
public enum TemperatureType implements QuantityType {
    /** Градус Цельсия. */
    CELSIUS,
    /** Градус Фаренгейта. */
    FAHRENHEIT,
    /** Кельвин — термодинамическая шкала, отсчёт от абсолютного нуля. */
    KELVIN
}
