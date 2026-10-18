package ru.urfu.converterbot.bots.telegram;


import ru.urfu.converterbot.keyboard.model.KeyboardView;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**                                                                                                                                                                                    16,593 tokens
 * Конвертирует платформенно-независимую KeyboardView в InlineKeyboardMarkup Telegram.                                                                                                 8% used
 */
public class TelegramKeyboardRenderer {

    private TelegramKeyboardRenderer() {}

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
