package com.example.echobot.service;

import com.example.echobot.service.models.QuantityModel;
import com.example.echobot.service.models.QuantityType;

import java.util.Arrays;
import java.util.List;

/**
 * Конвертер одной группы величин: валюты, длины, веса или температуры.
 * Каждая группа живет в своём enum и в своём конвертере, поэтому добавление
 * новой группы не требует изменений в диспетчере.
 */
public interface QuantityConverter {

    /** Название группы величин для меню и текста справки. */
    String title();

    /** Команда, по которой бот показывает справку по этой группе. */
    String command();

    /** Enum с единицами группы. По нему определяется, кому принадлежит запрос. */
    Class<? extends QuantityType> quantityType();

    /**
     * Выполняет перевод по разобранному запросу.
     *
     * @param request запрос с исходным значением и обеими единицами одной группы
     * @return переведённое значение
     */
    double convert(QuantityModel request);

    /** Текст со справкой по коэффициентам группы. */
    String getActualCourse();

    /** Единицы группы в порядке объявления в enum. */
    default List<String> units() {
        return Arrays.stream(quantityType().getEnumConstants())
                .map(unit -> ((Enum<?>) unit).name())
                .toList();
    }

    /** Короткая подпись единицы для кнопки. По умолчанию — имя константы. */
    default String displayName(String unit) {
        return unit;
    }
}
