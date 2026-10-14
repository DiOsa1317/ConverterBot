package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.QuantityType;

public interface Parser {
    ConversionRequest<QuantityType> parse(String message);
}
