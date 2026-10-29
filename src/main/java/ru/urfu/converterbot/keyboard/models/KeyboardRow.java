package ru.urfu.converterbot.keyboard.models;

import java.util.List;

/**
 * Описывает одну строку кнопок клавиатуры.
 *
 * @param buttons список кнопок строки
 */
public record KeyboardRow(List<KeyboardButton> buttons) {
}
