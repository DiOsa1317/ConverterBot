package ru.urfu.converterbot.keyboard.models;

/**
 * Описывает одну кнопку клавиатуры.
 *
 * @param label текст, отображаемый на кнопке
 * @param actionData данные, передаваемые боту при нажатии
 */
public record KeyboardButton(String label, String actionData) { }
