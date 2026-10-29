package ru.urfu.converterbot.keyboard.renderers;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.converterbot.keyboard.models.KeyboardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Конвертирует платформенно-независимую KeyboardView в InlineKeyboardMarkup Telegram.
 */
public class TelegramKeyboardRenderer {

     /**
     * Преобразует представление клавиатуры в формат Telegram API.
     *
     * @param view описание клавиатуры
     * @return inline-клавиатура или {@code null}, если клавиатура отсутствует
     *         либо не содержит строк
     */
    public InlineKeyboardMarkup render(KeyboardView view) {
        if (view == null || view.isEmpty()) {
            return null;
        }
        var markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (var row : view.rows()) {
            List<InlineKeyboardButton> telegramRow = new ArrayList<>();
            for (var button : row.buttons()) {
                telegramRow.add(InlineKeyboardButton.builder()
                        .text(button.label())
                        .callbackData(button.actionData())
                        .build());
            }
            rows.add(telegramRow);
        }
        markup.setKeyboard(rows);
        return markup;
    }
}