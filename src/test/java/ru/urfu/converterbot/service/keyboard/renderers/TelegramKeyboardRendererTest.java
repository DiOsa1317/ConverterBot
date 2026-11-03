package ru.urfu.converterbot.service.keyboard.renderers;

import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.models.KeyboardButton;
import ru.urfu.converterbot.keyboard.models.KeyboardRow;
import ru.urfu.converterbot.keyboard.models.KeyboardView;
import ru.urfu.converterbot.keyboard.renderers.TelegramKeyboardRenderer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelegramKeyboardRendererTest {
    private final TelegramKeyboardRenderer renderer = new TelegramKeyboardRenderer();

    @Test
    void renderMainMenu() {
        var keyboard = new KeyboardView(List.of(new KeyboardRow(List.of(
                new KeyboardButton("Валюты", "TYPE_CURRENCY"),
                new KeyboardButton("Длина", "TYPE_LENGTH"),
                new KeyboardButton("Вес", "TYPE_WEIGHT"),
                new KeyboardButton("Температура", "TYPE_TEMPERATURE")))
        ));

        var markup = renderer.render(keyboard);
        assertNotNull(markup);
        assertEquals(1, markup.getKeyboard().size());
        assertEquals(4, markup.getKeyboard().get(0).size());
    }

    @Test
    void renderEmptyReturnsNull() {
        assertNull(renderer.render(null));
        assertNull(renderer.render(new KeyboardView(null)));
    }
}
