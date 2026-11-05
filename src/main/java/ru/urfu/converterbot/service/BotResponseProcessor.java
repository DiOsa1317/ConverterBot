package ru.urfu.converterbot.service;

/**
 * Интерфейс для реализации бизнес-логики бота
 */
public interface BotResponseProcessor {
    /**
     * Обрабатывает сообщение пользователя.
     * @param messageText входное сообщение пользователя, либо {@code null},
     *                    если сообщение не содержит текста
     * @return текст — результат обработки запроса или сообщение об ошибке
     */
    String processBotResponse(String messageText);
}
