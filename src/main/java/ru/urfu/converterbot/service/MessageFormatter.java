package ru.urfu.converterbot.service;

import java.math.BigDecimal;
import java.util.Map;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

import java.time.LocalDateTime;

public interface MessageFormatter {
    /**
     * Возвращает приветствие.
     *
     * @return текст приветствия
     */
    String start();

    /**
     * Возвращает общую справку.
     *
     * @return текст справки
     */
    String help();

    /**
     * Возвращает ответ на сообщение без текста.
     *
     * @return текст ответа
     */
    String noText();

    /**
     * Возвращает ответ на сообщение в неверном формате.
     *
     * @return текст ответа
     */
    String invalidInput();

    /**
     * Возвращает ответ на непредвиденную ошибку.
     *
     * @return текст ответа
     */
    String unexpectedError();

    /**
     * Формирует справку о соотношениях единиц группы.
     *
     * @param title заголовок справки
     * @param baseUnit базовая единица группы
     * @param rates сколько единиц содержится в одной базовой
     * @return текст справки
     */
    String rates(String title, QuantityType baseUnit, Map<QuantityType, BigDecimal> rates);

    /**
     * Формирует текст с результатом.
     *
     * @param request разобранный запрос пользователя
     * @param result результат запроса
     * @return текст ответа
     */
    String getResult(ConversionRequest request, BigDecimal result);

    /**
     * Формирует справку по курсам валют.
     *
     * @param baseUnit    базовая валюта
     * @param rates       курсы валют
     * @param lastUpdated время последнего обновления курсов
     * @return текст справки
     */
    String currencyRates(QuantityType baseUnit, Map<QuantityType, BigDecimal> rates,
            LocalDateTime lastUpdated);
}
