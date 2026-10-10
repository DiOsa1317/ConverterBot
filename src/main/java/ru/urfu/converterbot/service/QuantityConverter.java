package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Конвертер одной группы величин: валюты, длины, веса или температуры.
 * Каждая группа живет в своём enum и в своём конвертере, поэтому добавление
 * новой группы не требует изменений в диспетчере.
 */
public interface QuantityConverter {

    /**
     * Возвращает название группы величин для меню и текста справки.
     *
     * @return название группы, например «Валюты»
     */
    String title();

    /**
     * Возвращает команду, по которой бот показывает справку по этой группе.
     *
     * @return команда с ведущим слэшем, например {@code /currency}
     */
    String command();

    /**
     * Возвращает enum с единицами группы. По нему определяется, кому принадлежит запрос.
     *
     * @return класс enum, реализующий {@link QuantityType}
     */
    Class<? extends QuantityType> quantityType();

    /**
     * Выполняет перевод по разобранному запросу.
     *
     * @param request запрос с исходным значением и обеими единицами одной группы
     * @return переведённое значение
     */
    BigDecimal convert(ConversionRequest request);

    /**
     * Возвращает текст со справкой по коэффициентам группы.
     *
     * @return текст справки для отправки в чат
     */
    String getUnitsDescription();

    /**
     * Возвращает единицы группы в порядке объявления в enum.
     *
     * @return список имён констант группы
     */
    default List<String> units() {
        return Arrays.stream(quantityType().getEnumConstants())
                .map(unit -> ((Enum<?>) unit).name())
                .toList();
    }

    /**
     * Возвращает короткую подпись единицы для кнопки меню.
     *
     * @param unit имя единицы, например {@code MILE}
     * @return подпись для интерфейса; по умолчанию — имя константы
     */
    default String displayName(String unit) {
        return unit;
    }
}
