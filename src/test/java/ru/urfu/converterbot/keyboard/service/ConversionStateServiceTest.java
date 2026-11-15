package ru.urfu.converterbot.keyboard.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты сервиса состояний {@link ConversionStateService}.
 */
class ConversionStateServiceTest {
    /**
     * Проверяет дефолтное состояние чата.
     */
    @Test
    @DisplayName("Дефолтное состояние — IDLE")
    void defaultState() {
        var svc = new ConversionStateService();
        assertEquals(State.IDLE, svc.getChatContext(1L).state());
    }

    /**
     * Проверяет сохранение и сброс состояния.
     */
    @Test
    @DisplayName("updateContext сохраняет, reset удаляет")
    void updateAndReset() {
        var svc = new ConversionStateService();
        var ctx = new ChatContext(State.AWAITING_NUMBER, "CURRENCY", null, null, null);
        svc.updateContext(1L, ctx);
        assertEquals(State.AWAITING_NUMBER, svc.getChatContext(1L).state());
        svc.reset(1L);
        assertEquals(State.IDLE, svc.getChatContext(1L).state());
    }
}
