package ru.urfu.converterbot.keyboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.models.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты фабрики клавиатур {@link KeyboardFactory}.
 */
class KeyboardFactoryTest {
    /** Фабрика клавиатур, используемая во всех тестах. */
    private final KeyboardFactory factory = new KeyboardFactory();

    /**
     * Проверяет состав главного меню.
     */
    @Test
    @DisplayName("Главное меню: 4 категории в 2 строках, без кнопки 'Меню'")
    void mainMenu() {
        var view = factory.createMainMenu();
        assertFalse(view.isEmpty());
        assertEquals(2, view.rows().size());
        var labels = view.rows().stream().flatMap(r -> r.buttons().stream())
                .map(KeyboardButton::label).toList();
        assertTrue(labels.containsAll(List.of("Валюты", "Температура", "Вес", "Длина")));
    }

    /**
     * Проверяет клавиатуру выбора исходной единицы.
     */
    @Test
    @DisplayName("Клавиатура исходной единицы содержит все единицы и кнопку 'Меню'")
    void sourceKeyboard() {
        var view = factory.createSourceUnitKeyboard("LENGTH");
        var labels = view.rows().stream().flatMap(r -> r.buttons().stream())
                .map(KeyboardButton::label).toList();
        assertEquals(List.of("m", "km", "mi", "cm", "in", "Меню"), labels);
    }

    /**
     * Проверяет исключение исходной единицы из целевой клавиатуры.
     */
    @Test
    @DisplayName("Клавиатура целевой единицы исключает исходную")
    void targetKeyboard() {
        var view = factory.createTargetUnitKeyboard("LENGTH", "km");
        var labels = view.rows().stream().flatMap(r -> r.buttons().stream())
                .map(KeyboardButton::label).toList();
        assertFalse(labels.contains("km"));
        assertTrue(labels.contains("m"));
        assertTrue(labels.contains("Меню"));
    }

    /**
     * Проверяет выбор клавиатуры по состоянию диалога.
     */
    @Test
    @DisplayName("createFor возвращает клавиатуру для каждого состояния")
    void createFor() {
        assertNotNull(factory.createFor(new ChatContext(State.IDLE, null, null, null, null)));
        assertNotNull(factory.createFor(new ChatContext(State.AWAITING_SOURCE, "LENGTH", null, null, null)));
        assertNotNull(factory.createFor(new ChatContext(State.AWAITING_TARGET, "LENGTH", "10", "m", null)));
    }

    /**
     * Проверяет получение единиц по категории.
     */
    @Test
    @DisplayName("unitsOf: известная категория / неизвестная")
    void unitsOf() {
        assertEquals(5, factory.unitsOf("LENGTH").size());
        assertTrue(factory.unitsOf("UNKNOWN").isEmpty());
    }
}
