package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

/**
 * Интерфейс парсера для извлечения формата запроса на конвертацию.
 */
public interface Parser {
    ConversionRequest<QuantityType> parse(String message);
}
