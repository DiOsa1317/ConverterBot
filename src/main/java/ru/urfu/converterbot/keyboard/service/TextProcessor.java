package ru.urfu.converterbot.keyboard.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.BotReply;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotResponseProcessor;
import ru.urfu.converterbot.service.MessageFormatter;

/** Обработчик текстовых сообщений пользователя*/
public class TextProcessor implements MessageProcessor {

    private static final String MENU_COMMAND = "/menu";
    private static final String START_COMMAND = "/start";
    private static final String HELP_COMMAND = "/help";

    private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000");

    private static final String INVALID_NUMBER_TEXT = "Не удалось распознать число. ";
    private static final String NUMBER_EXAMPLE_TEXT = "Введите число. Например: 100";
    private static final String MENU_HINT_TEXT = "Используйте кнопки меню для выбора типа перевода.";

    private final ConversionStateService stateService;

    private final KeyboardFactory keyboardFactory;

    private final BotResponseProcessor processor;

    private final MessageFormatter formatter;

    /**
     * Создаёт сервис диалога.
     *
     * @param stateService хранилище состояний
     * @param keyboardFactory фабрика клавиатур
     * @param processor готовит ответ бота
     */
    public TextProcessor(@NotNull ConversionStateService stateService,
                                 @NotNull KeyboardFactory keyboardFactory,
                                 @NotNull BotResponseProcessor processor,
                         @NotNull MessageFormatter messageFormatter) {
        this.stateService = stateService;
        this.keyboardFactory = keyboardFactory;
        this.processor = processor;
        this.formatter = messageFormatter;
    }

    @Override
    public BotReply handle(long chatId, @Nullable String text) {
        var context = stateService.getChatContext(chatId);
        if (text == null) {
            if (context.state() == State.AWAITING_NUMBER) {
                return new BotReply(NUMBER_EXAMPLE_TEXT);
            }
            return new BotReply(MENU_HINT_TEXT, keyboardFactory.createMainMenu());
        }
        if (text.equalsIgnoreCase(START_COMMAND)) {
            stateService.reset(chatId);
            return new BotReply(formatter.start() + MENU_HINT_TEXT, keyboardFactory.createMainMenu());
        }

        if (text.equalsIgnoreCase(MENU_COMMAND)) {
            stateService.reset(chatId);
            return replyFor(stateService.getChatContext(chatId));
        }

        if (text.equalsIgnoreCase(HELP_COMMAND)) {
            stateService.reset(chatId);
            return new BotReply(formatter.help() + MENU_HINT_TEXT, keyboardFactory.createMainMenu());
        }

        if (context.state() == State.AWAITING_NUMBER) {
            if (text.isBlank()) {
                return new BotReply(NUMBER_EXAMPLE_TEXT);
            }

            return handleNumber(chatId, context, text);
        }

        if (text.isBlank()) {
            return new BotReply(
                    MENU_HINT_TEXT,
                    keyboardFactory.createMainMenu()
            );
        }

        return new BotReply(
                processor.processBotResponse(text) + MENU_HINT_TEXT,
                keyboardFactory.createMainMenu()
        );
    }

    /** Обрабатывает сообщение-число
     * @param chatId - id чата
     * @param context - контекст чата
     * @param text - сообщение пользователя
     * @return ответ бота
     * */
    private BotReply handleNumber(long chatId, ChatContext context, String text) {
        var number = parseNumber(text);
        if (number.isEmpty()) {
            return new BotReply(INVALID_NUMBER_TEXT + promptFor(context), null);
        }
        return moveTo(chatId, new ChatContext(State.AWAITING_SOURCE,
                context.conversionType(), number.get().toPlainString(), null, null));
    }

    /** Превращает число-строку в число
     * @param text - строка, приводимая к числу
     * @return число после парсинга или Optional.empty(), если ввод не подходит под критерии
     * */
    private Optional<BigDecimal> parseNumber(String text) {
        try {
            var value = new BigDecimal(text.trim().replace(',', '.'));
            var inRange = value.signum() >= 0 && value.compareTo(MAX_VALUE) < 0;
            return inRange ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    /** Возвращает ответ для пользователя и перемещает на следующий этап
     * @param chatId - id чата
     * @param next - следующий контекст чата
     * @return ответ бота
     * */
    private BotReply moveTo(long chatId, ChatContext next) {
        stateService.updateContext(chatId, next);
        return replyFor(next);
    }

    /** Подбирает ответ для пользователя в зависимости от контекста
     * @param context - следующий контекст чата
     * @return ответ бота
     * */
    private BotReply replyFor(ChatContext context) {
        return new BotReply(promptFor(context), keyboardFactory.createFor(context));
    }

    /** Подбирает строковую часть ответа для пользователя в зависимости от контекста
     * @param context - следующий контекст чата
     * @return строковая часть ответа
     * */
    private String promptFor(ChatContext context) {
        return switch (context.state()) {
            case IDLE -> "Выберите тип перевода:";
            case AWAITING_NUMBER -> "Введите число x, где 0 <= x < 10^9. "
                    + "Дробные числа вводите через точку.";
            case AWAITING_SOURCE -> "Выберите стартовую единицу:";
            case AWAITING_TARGET -> "Выберите конечную единицу:";
        };
    }
}
