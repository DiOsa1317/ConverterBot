package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.WeightType;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class WeightConverter implements QuantityConverter {
    /** Сколько метров содержится в одной единице измерения. */
    private final Map<WeightType, Double> kilogramsInUnit = new EnumMap<>(WeightType.class);

    public WeightConverter() {
        kilogramsInUnit.put(WeightType.KILOGRAM, 1.0);
        kilogramsInUnit.put(WeightType.TON, 1000.0);
        kilogramsInUnit.put(WeightType.GRAM, 0.001);
        kilogramsInUnit.put(WeightType.POUND, 0.45359237);
    }

    @Override
    public String title() {
        return "Вес";
    }

    @Override
    public String command() {
        return "/weight";
    }

    @Override
    public Class<WeightType> quantityType() {
        return WeightType.class;
    }

    @Override
    public double convert(QuantityModel request) {
        var from = (WeightType) request.from();
        var to = (WeightType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value() * kilogramsInUnit.get(from) / kilogramsInUnit.get(to);
    }

    @Override
    public String getActualCourse() {
        return String.format(Locale.US, """
                Величины весов относительно килограммов:
                Грамм = %f кг;
                Фунт = %f кг;
                Тонна = %f кг;
                """,
                kilogramsInUnit.get(WeightType.GRAM),
                kilogramsInUnit.get(WeightType.POUND),
                kilogramsInUnit.get(WeightType.TON));
    }
}
