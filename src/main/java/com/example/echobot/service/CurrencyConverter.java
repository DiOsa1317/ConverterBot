package com.example.echobot.service;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.QuantityModel;

/**
 * Конвертер валют для обработки запросов на конвертацию.
 * Использует фиксированные курсы относительно рубля (RUB) как базовой валюты.
 */
public class CurrencyConverter implements QuantityConverter {

    /** Карта курсов валют относительно рубля (сколько рублей стоит 1 единица валюты) */
    private final Map<CurrencyType, Double> ratesToRub = new EnumMap<>(CurrencyType.class);

    /**
     * Создаёт конвертер и заполняет таблицу курсов валют к рублю.
     */
    public CurrencyConverter() {
        ratesToRub.put(CurrencyType.RUB, 1.0);
        ratesToRub.put(CurrencyType.USD, 84.3414);
        ratesToRub.put(CurrencyType.EUR, 95.8709);
        ratesToRub.put(CurrencyType.CNY, 12.5355);
        ratesToRub.put(CurrencyType.KZT, 1.91);
    }

    /**
     * Возвращает название группы для меню и текста справки.
     *
     * @return название группы — «Валюты»
     */
    @Override
    public String title() {
        return "Валюты";
    }

    /**
     * Возвращает команду, по которой бот показывает справку по группе.
     *
     * @return команда {@code /currency}
     */
    @Override
    public String command() {
        return "/currency";
    }

    /**
     * Возвращает enum с единицами группы — по нему реестр определяет владельца запроса.
     *
     * @return класс enum с типами валют
     */
    @Override
    public Class<CurrencyType> quantityType() {
        return CurrencyType.class;
    }

    /**
     * Переводит валюту через рубль как базовую: значение умножается на курс
     * исходной валюты и делится на курс целевой.
     *
     * @param request запрос с исходным значением и обеими валютами группы
     * @return переведённое значение; при одинаковых валютах — исходное значение
     */
    @Override
    public double convert(QuantityModel request) {
        var from = (CurrencyType) request.from();
        var to = (CurrencyType) request.to();
        if (from == to) {
            return request.value();
        }
        return request.value() * ratesToRub.get(from) / ratesToRub.get(to);
    }

    /**
     * Возвращает текст со списком действующих курсов валют к рублю.
     *
     * @return справка с курсами долара, евро, юаня и тенге
     */
    @Override
    public String getActualCourse() {
        return String.format(Locale.US, """
                Доступны переводы по таким курсам:
                Рубли к долларам - %.4f;
                Рубли к евро - %.4f;
                Рубли к юаням - %.4f;
                Рубли к тенге - %.4f;
                """,
                ratesToRub.get(CurrencyType.USD),
                ratesToRub.get(CurrencyType.EUR),
                ratesToRub.get(CurrencyType.CNY),
                ratesToRub.get(CurrencyType.KZT));
    }
}