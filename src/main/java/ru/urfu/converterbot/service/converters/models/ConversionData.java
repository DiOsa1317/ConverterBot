package ru.urfu.converterbot.service.converters.models;

import ru.urfu.converterbot.service.models.QuantityType;

import java.math.BigDecimal;
import java.util.Map;

/** Модель для хранения курсов величин */
public interface ConversionData {

    /** Коэффициенты перевода в базовую единицу. */
    Map<QuantityType, BigDecimal> rates();
}