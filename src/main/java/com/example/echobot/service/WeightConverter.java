package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.WeightType;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Конвертер массы. Все переводы идут через килограмм как базовую единицу,
 * поэтому перевод в обе стороны симметричен и не требует таблицы пар.
 */
public class WeightConverter implements QuantityConverter {
    /** Сколько килограммов содержится в одной единице измерения. */
    private final Map<WeightType, Double> kilogramsInUnit = new EnumMap<>(WeightType.class);

    /**
     * Создаёт конвертер и заполняет таблицу переводов единиц в килограммы.
     */
    public WeightConverter() {
        kilogramsInUnit.put(WeightType.KILOGRAM, 1.0);
        kilogramsInUnit.put(WeightType.TON, 1000.0);
        kilogramsInUnit.put(WeightType.GRAM, 0.001);
        kilogramsInUnit.put(WeightType.POUND, 0.45359237);
    }

    /**
     * Возвращает название группы для меню и текста справки.
     *
     * @return название группы — «Вес»
     */
    @Override
    public String title() {
        return "Вес";
    }

    /**
     * Возвращает команду, по которой бот показывает справку по группе.
     *
     * @return команда {@code /weight}
     */
    @Override
    public String command() {
        return "/weight";
    }

    /**
     * Возвращает enum с единицами группы — по нему реестр определяет владельца запроса.
     *
     * @return класс enum с типами массы
     */
    @Override
    public Class<WeightType> quantityType() {
        return WeightType.class;
    }

    /**
     * Переводит массу через килограммы: значение умножается на коэффициент
     * исходной единицы и делится на коэффициент целевой.
     *
     * @param request запрос с исходным значением и обеими единицами группы
     * @return переведённое значение; при одинаковых единицах — исходное значение
     */
    @Override
    public double convert(QuantityModel request) {
        var from = (WeightType) request.from();
        var to = (WeightType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value() * kilogramsInUnit.get(from) / kilogramsInUnit.get(to);
    }

    /**
     * Возвращает текст со списком единиц группы в килограммах.
     *
     * @return справка с переводом грамма, фунта и тонны в килограммы
     */
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
