package ru.urfu.converterbot.keyboard.models;
import java.util.List;

/**
 * Платформенно-независимое представление клавиатуры.
 */
public record KeyboardView(List<KeyboardRow> rows) {

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}
