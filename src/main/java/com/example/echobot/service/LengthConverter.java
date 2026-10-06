package com.example.echobot.service;

import com.example.echobot.service.models.LengthType;
import com.example.echobot.service.models.QuantityModel;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Конвертер длины. Все переводы идут через метры как базовую единицу,
 * поэтому перевод в обе стороны симметричен и не требует таблицы пар.
 */
public class LengthConverter implements QuantityConverter{

    /** Сколько метров содержится в одной единице измерения. */
    private final Map<LengthType, Double> metersInUnit = new EnumMap<>(LengthType.class);

   public LengthConverter()  {
        metersInUnit.put(LengthType.METER, 1.0);
        metersInUnit.put(LengthType.KILOMETER, 1000.0);
        metersInUnit.put(LengthType.MILE, 1609.344);
        metersInUnit.put(LengthType.CENTIMETER, 0.01);
        metersInUnit.put(LengthType.INCH, 0.0254);
    }

    @Override
    public String title() {
        return "Длина";
    }

    @Override
    public String command() {
        return "/length";
    }

    @Override
    public Class<LengthType> quantityType() {
        return LengthType.class;
    }

    @Override
    public double convert(QuantityModel request) {
        var from = (LengthType) request.from();
        var to = (LengthType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value() * metersInUnit.get(from) / metersInUnit.get(to);
    }

    @Override
    public String getActualCourse() {
        return String.format(Locale.US, """
                Величины длин относительно метров:
                Километр = %f м;
                Миля = %f м;
                Дюйм = %f м;
                """,
                metersInUnit.get(LengthType.KILOMETER),
                metersInUnit.get(LengthType.MILE),
                metersInUnit.get(LengthType.INCH));
    }
}
