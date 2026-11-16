package ru.urfu.converterbot.service.models;

import java.time.LocalDateTime;

/**
 * Запись об одной выполненной конвертации.
 */
public record ConversionHistory(
        long id,
        long chatId,
        String input,
        String result,
        LocalDateTime createdAt
) {
}