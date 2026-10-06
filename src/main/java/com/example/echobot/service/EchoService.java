package com.example.echobot.service;

/**
 * Сервис, обрабатывающий входящие сообщения путем отправления тех же самых сообщений.
 */
public class EchoService implements BotResponseProcessor {

    /**
     * Возвращает исходный текст сообщения или уведомление, если текст пуст.
     *
     * @param messageText исходный текст от пользователя
     * @return обработанный текст
     */
    public String processBotResponse(String messageText) {
        if (messageText.isBlank()) {
            return "Сообщение не должно быть пустым";
        }
        return messageText;
    }
}