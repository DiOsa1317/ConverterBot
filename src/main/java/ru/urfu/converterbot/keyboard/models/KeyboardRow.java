package ru.urfu.converterbot.keyboard.models;


import java.util.List;

public record KeyboardRow(List<KeyboardButton> buttons) {

    public static KeyboardRow single(KeyboardButton button) {
        return new KeyboardRow(List.of(button));
    }

}
