package ru.urfu.converterbot.keyboard.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ответ бота: текст и, возможно, клавиатура под ним.
 *
 * @param text текст сообщения
 * @param keyboard клавиатура или {@code null}, если кнопки не нужны
 */
public record BotReply(@NotNull String text, @Nullable KeyboardView keyboard) {
}