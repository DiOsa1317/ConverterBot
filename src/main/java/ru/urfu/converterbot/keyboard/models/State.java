package ru.urfu.converterbot.keyboard.models;

/** Текущее состояние диалога */
public enum State {
    IDLE,
    AWAITING_NUMBER,
    AWAITING_SOURCE,
    AWAITING_TARGET,
    AWAITING_CUSTOM_FROM,
    AWAITING_CUSTOM_TO,
    AWAITING_CUSTOM_RATE,
    AWAITING_CUSTOM_CONVERT_NUMBER,
    CUSTOM_CONVERSIONS_MENU
}
