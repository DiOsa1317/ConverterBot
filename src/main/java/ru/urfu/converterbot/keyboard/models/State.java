package ru.urfu.converterbot.keyboard.models;

/** Текущее состояние диалога */
public enum State {
    IDLE,
    AWAITING_NUMBER,
    AWAITING_SOURCE,
    AWAITING_TARGET
}
