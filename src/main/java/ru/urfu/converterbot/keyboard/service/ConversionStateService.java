package ru.urfu.converterbot.keyboard.service;

import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConversionStateService {
    private final Map<Long, ChatContext> chatStates = new ConcurrentHashMap<>();

    /**
     * Получить текущее состояние чата.
     */
    public ChatContext getChatContext(Long chatId) {
        return chatStates.getOrDefault(chatId, new ChatContext(State.IDLE,
                null, null, null, null));
    }

    /**
     * Обновить состояние чата.
     */
    public void updateContext(Long chatId, ChatContext context) {
        chatStates.put(chatId, context);
    }

    /**
     * Сбросить состояние чата.
     */
    public void reset(Long chatId) {
        chatStates.remove(chatId);
    }
}
