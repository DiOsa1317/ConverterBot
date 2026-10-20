package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Конвертер группы величин.
 */
public interface QuantityConverter {

    /**
     * Возвращает базовую единицу группы, относительно которой строится справка.
     *
     * @return базовая единица, например метр для длины
     */
    QuantityType baseUnit();

    /**
     * Переводит значение из одной единицы в другую.
     *
     * @param value исходное значение
     * @param from единица, из которой переводим
     * @param to единица, в которую переводим
     * @return переведённое значение
     */
    BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to);

    /**
     * Возвращает соотношения единиц группы.
     *
     * @return для каждой единицы группы, кроме базовой: сколько таких единиц
     *         содержится в одной базовой; порядок как в объявлении enum
     */
    Map<QuantityType, BigDecimal> getReferenceRates();
}
