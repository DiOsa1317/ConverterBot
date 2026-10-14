package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;

/**
 * Конвертер одной группы величин: валюты, длины, веса или температуры.
 * Каждая группа живет в своём enum и в своём конвертере, поэтому добавление
 * новой группы не требует изменений в диспетчере.
 */
public interface QuantityConverter<T extends QuantityType> {

    /**
     * Выполняет перевод по разобранному запросу.
     *
     * @param request запрос с исходным значением и обеими единицами одной группы
     * @return переведённое значение
     */
    BigDecimal convert(ConversionRequest<T> request);

    /**
     * Возвращает текст со справкой по коэффициентам группы.
     *
     * @return текст справки для отправки в чат
     */
    String getUnitsDescription();
}
