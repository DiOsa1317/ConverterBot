package ru.urfu.converterbot.keyboard.service;

import org.jetbrains.annotations.Nullable;

import ru.urfu.converterbot.keyboard.models.BotReply;

/**
 * Определяет общий интерфейс обработки входных данных пользователя.
 */
public interface MessageProcessor {

     /**
     * Обрабатывает входные данные и формирует ответ.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения или данные действия
     * @return ответ бота
     */
    BotReply handle(long chatId, @Nullable String text);
}
