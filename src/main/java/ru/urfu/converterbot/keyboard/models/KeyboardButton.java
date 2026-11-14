package ru.urfu.converterbot.keyboard.models;

import org.jetbrains.annotations.NotNull;

/**
 * Описывает одну кнопку клавиатуры.
 *
 * @param label текст, отображаемый на кнопке
 * @param action тип действия кнопки
 * @param value значение действия, например код категории или единицы
 */
public record KeyboardButton(
        @NotNull String label,
        @NotNull ButtonAction action,
        @NotNull String value) {

    /**
     * Возвращает данные, которые будут переданы Telegram при нажатии кнопки.
     *
     * @return строка callback-данных
     */
    public String callbackData() {
        return switch (action) {
            case TYPE -> "TYPE:" + value;
            case FROM -> "FROM:" + value;
            case TO -> "TO:" + value;
            case MENU -> "MENU";
            case CUSTOM_CONVERSIONS -> "CUSTOM_CONVERSIONS";
            case ADD_CUSTOM_CONV -> "ADD_CUSTOM_CONV";
            case LIST_CUSTOM_CONV -> "LIST_CUSTOM_CONV";
            case DELETE_CUSTOM_CONV -> "DELETE_CUSTOM_CONV:" + value;
        };
    }
 }
