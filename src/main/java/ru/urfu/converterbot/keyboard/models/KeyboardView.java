package ru.urfu.converterbot.keyboard.models;
import java.util.List;

/**
 * Платформенно-независимое представление клавиатуры.
 * 
 * @param rows строки кнопок
 */
public record KeyboardView(List<KeyboardRow> rows) {

     /**
     * Проверяет, содержит ли клавиатура хотя бы одну строку.
     *
     * @return {@code true}, если строк нет или список равен {@code null}
     */
    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}
