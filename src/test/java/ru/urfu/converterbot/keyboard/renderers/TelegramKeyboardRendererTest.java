package ru.urfu.converterbot.keyboard.renderers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.KeyboardView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты рендерера клавиатур {@link TelegramKeyboardRenderer}.
 */
class TelegramKeyboardRendererTest {
    /** Рендерер, используемый во всех тестах. */
    private final TelegramKeyboardRenderer renderer = new TelegramKeyboardRenderer();
    /** Фабрика клавиатур для подготовки данных. */
    private final KeyboardFactory factory = new KeyboardFactory();

    /**
     * Проверяет возврат null для null и пустой клавиатуры.
     */
    @Test
    @DisplayName("null и пустая клавиатура -> null")
    void nullOrEmpty() {
        assertNull(renderer.render(null));
        assertNull(renderer.render(new KeyboardView(null)));
        assertNull(renderer.render(new KeyboardView(List.of())));
    }

    /**
     * Проверяет рендеринг непустой клавиатуры.
     */
    @Test
    @DisplayName("Непустая клавиатура -> InlineKeyboardMarkup со строками")
    void renders() {
        var markup = renderer.render(factory.createMainMenu());
        assertNotNull(markup);
        assertEquals(3, markup.getKeyboard().size());
    }
}
