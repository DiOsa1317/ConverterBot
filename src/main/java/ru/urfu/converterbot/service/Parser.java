package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;

public interface Parser {
    ConversionRequest parse(String message);
}
