package ru.urfu.converterbot.keyboard.service;

import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит состояние диалога для каждого чата.
 */
public class ConversionStateService {
    private final Map<Long, ChatContext> chatStates = new ConcurrentHashMap<>();

    /**
     * Получить текущее состояние чата.
     */
    public ChatContext getChatContext(Long chatId) {
        ChatContext context = chatStates.get(chatId);

        if (context == null) {
            return new ChatContext(State.IDLE, null, null, null, null);
        }

        return context;
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
