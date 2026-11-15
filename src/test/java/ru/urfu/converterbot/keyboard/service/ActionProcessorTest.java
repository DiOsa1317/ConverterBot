package ru.urfu.converterbot.keyboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты обработчика нажатий на клавиатуру {@link ActionProcessor}.
 */
class ActionProcessorTest {
    /** Сервис состояний диалога. */
    private ConversionStateService stateService;
    /** Обработчик действий, создаваемый перед каждым тестом. */
    private ActionProcessor processor;

    /**
     * Создаёт сервисы перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        stateService = new ConversionStateService();
        processor = new ActionProcessor(stateService, new KeyboardFactory(), msg -> "RESULT:" + msg);
    }

    /**
     * Проверяет выбор типа конвертации.
     */
    @Test
    @DisplayName("TYPE:CURRENCY -> AWAITING_NUMBER")
    void typeSelection() {
        var reply = processor.handle(1L, "TYPE:CURRENCY");
        assertEquals(State.AWAITING_NUMBER, stateService.getChatContext(1L).state());
        assertTrue(reply.text().contains("Введите число"));
    }

    /**
     * Проверяет выбор исходной единицы.
     */
    @Test
    @DisplayName("FROM -> AWAITING_TARGET")
    void fromSelection() {
        stateService.updateContext(1L, new ChatContext(State.AWAITING_SOURCE, "CURRENCY", "100", null, null));
        var reply = processor.handle(1L, "FROM:USD");
        assertEquals(State.AWAITING_TARGET, stateService.getChatContext(1L).state());
        assertTrue(reply.text().contains("конечную"));
    }

    /**
     * Проверяет конвертацию при выборе целевой единицы.
     */
    @Test
    @DisplayName("TO -> конвертация и сброс состояния")
    void toSelection() {
        stateService.updateContext(1L, new ChatContext(State.AWAITING_TARGET, "CURRENCY", "100", "USD", null));
        var reply = processor.handle(1L, "TO:EUR");
        assertTrue(reply.text().contains("RESULT:"));
        assertNull(reply.keyboard());
        assertEquals(State.IDLE, stateService.getChatContext(1L).state());
    }

    /**
     * Проверяет сообщение об устаревшей кнопке.
     */
    @Test
    @DisplayName("Устаревшая кнопка -> сообщение об устаревании")
    void stale() {
        stateService.reset(1L);
        var reply = processor.handle(1L, "FROM:USD");
        assertTrue(reply.text().contains("устарела"));
    }

    /**
     * Проверяет возврат главного меню по команде MENU.
     */
    @Test
    @DisplayName("MENU -> главное меню")
    void menu() {
        var reply = processor.handle(1L, "MENU");
        assertTrue(reply.text().contains("Выберите тип перевода"));
        assertNotNull(reply.keyboard());
    }
}
