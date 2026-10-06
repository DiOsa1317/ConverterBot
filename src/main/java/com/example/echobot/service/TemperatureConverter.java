package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.QuantityType;
import com.example.echobot.service.models.TemperatureType;

/**
 * Конвертер температуры. Переводит через градусы Цельсия как промежуточную шкалу,
 * потому что перевод между °C, °F и K аффинный, а не ратный: результат нельзя
 * получить умножением исходного значения на коэффициент.
 */
public class TemperatureConverter implements QuantityConverter{

    private static final double FAHRENHEIT_IN_CELSIUS = 1.8;
    private static final double FAHRENHEIT_OFFSET = 32.0;
    private static final double ABSOLUTE_ZERO_IN_CELSIUS = 273.15;

    @Override
    public String title() {
        return "Температура";
    }

    @Override
    public String command() {
        return "/temperature";
    }

    @Override
    public Class<TemperatureType> quantityType() {
        return TemperatureType.class;
    }

    @Override
    public String displayName(String unit) {
        return switch (unit) {
            case "CELSIUS" -> "°C";
            case "FAHRENHEIT" -> "°F";
            default -> unit;
        };
    }

    @Override
    public double convert(QuantityModel request) {
        var from = (TemperatureType) request.from();
        var to = (TemperatureType) request.to();
        if (from == to) {
            return request.value();
        }
        return fromCelsius(toCelsius(request.value(), from), to);
    }

    /** Приводит значение в градусах Цельсия. */
    private double toCelsius(double value, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS ->  value;
            case FAHRENHEIT -> (value - FAHRENHEIT_OFFSET) / FAHRENHEIT_IN_CELSIUS;
            case KELVIN -> value - ABSOLUTE_ZERO_IN_CELSIUS;
        };
    }

    /** Выражает значение в градусах Цельсия в целевых единицах. */
    private double fromCelsius(double celsius, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS -> celsius;
            case FAHRENHEIT -> celsius * FAHRENHEIT_IN_CELSIUS + FAHRENHEIT_OFFSET;
            case KELVIN -> celsius + ABSOLUTE_ZERO_IN_CELSIUS;
        };
    }

    @Override
    public String getActualCourse() {
        return """
                Cвязь температур:
                °C → °F (значение * 1.8 + 32);
                °C → K (значение + 273.15);
                """;
    }
}
