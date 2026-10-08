package ru.urfu.converterbot.service;

/**
 * Интерфейс для реализации бизнес-логики бота
 */
public interface BotResponseProcessor {
    /**
     * Подготавливает ответ бота
     *
     * @param messageText исходный текст от пользователя
     * @return обработанный текст
     */
    String processBotResponse(String messageText);
}
