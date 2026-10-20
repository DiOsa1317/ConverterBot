package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.*;
import org.jetbrains.annotations.NotNull;

/**
 * Интерфейс парсера для извлечения формата запроса на конвертацию.
 */
public interface Parser {
    ConversionRequest parse(@NotNull String message) throws InvalidUserInputException;
}
