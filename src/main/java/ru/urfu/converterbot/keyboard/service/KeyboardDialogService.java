package ru.urfu.converterbot.keyboard.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.BotReply;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotResponseProcessor;

/**
 * Ведёт пошаговый диалог выбора перевода кнопками: тип, число, исходная и целевая единицы.
 * Не зависит от платформы: принимает текст и данные кнопок, возвращает {@link BotReply}.
 */
public class KeyboardDialogService {

    private static final String MENU_COMMAND = "/menu";
    private static final String START_COMMAND = "/start";

    private static final String START_TEXT = """
        Приветствую вас! Этот бот конвертирует валюты и физические величины.
        """;

    private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000");

    private static final String INVALID_NUMBER_TEXT = "Не удалось распознать число. ";
    private static final String STALE_BUTTON_TEXT =
            "Эта кнопка устарела. Введите /menu, чтобы начать заново.";

    private final ConversionStateService stateService;

    private final KeyboardFactory keyboardFactory;

    private final BotResponseProcessor processor;

    /**
     * Создаёт сервис диалога.
     *
     * @param stateService хранилище состояний
     * @param keyboardFactory фабрика клавиатур
     * @param processor обработчик текстовых запросов
     */
    public KeyboardDialogService(@NotNull ConversionStateService stateService,
                                 @NotNull KeyboardFactory keyboardFactory,
                                 @NotNull BotResponseProcessor processor) {
        this.stateService = stateService;
        this.keyboardFactory = keyboardFactory;
        this.processor = processor;
    }

    /**
     * Обрабатывает текстовое сообщение пользователя.
     *
     * @param chatId идентификатор чата
     * @param text текст сообщения или {@code null}, если текста нет
     * @return ответ для отправки в чат
     */
    public BotReply handleText(long chatId, @Nullable String text) {
        if (text != null && text.equalsIgnoreCase(START_COMMAND)) {
            stateService.reset(chatId);
            return new BotReply(START_TEXT, keyboardFactory.toMenu());
        }

        if (text != null && text.equalsIgnoreCase(MENU_COMMAND)) {
            stateService.reset(chatId);
            return replyFor(stateService.getChatContext(chatId));
        }

        var context = stateService.getChatContext(chatId);

        if (context.state() == State.AWAITING_NUMBER) {
            if (text == null) {
                return new BotReply(
                        "Введите число. Например: 100",
                        null
                );
            }

            return handleNumber(chatId, context, text);
        }

        return new BotReply(
                "Используйте кнопки меню для выбора действия.",
                keyboardFactory.createMainMenu()
        );
    }


    /**
     * Обрабатывает нажатие кнопки.
     *
     * @param chatId идентификатор чата
     * @param data данные нажатой кнопки
     * @return ответ для отправки в чат
     */
    public BotReply handleAction(long chatId, @NotNull String data) {
        var context = stateService.getChatContext(chatId);
        if (data.equals("MENU")) {
            stateService.reset(chatId);
            return replyFor(stateService.getChatContext(chatId));
        }
        if (data.startsWith(KeyboardFactory.TYPE_PREFIX)) {
            String type = data.substring(KeyboardFactory.TYPE_PREFIX.length());
            return moveTo(chatId, new ChatContext(State.AWAITING_NUMBER, type, null, null, null));
        }
        if (data.startsWith(KeyboardFactory.FROM_PREFIX)
                && context.state() == State.AWAITING_SOURCE) {
            var fromUnit = data.substring(KeyboardFactory.FROM_PREFIX.length());
            return moveTo(chatId, new ChatContext(State.AWAITING_TARGET,
                    context.conversionType(), context.number(), fromUnit, null));
        }
        if (data.startsWith(KeyboardFactory.TO_PREFIX)
                && context.state() == State.AWAITING_TARGET) {
            var toUnit = data.substring(KeyboardFactory.TO_PREFIX.length());
            stateService.reset(chatId);
            var query = context.number() + " " + context.fromUnit() + " to " + toUnit;
            return new BotReply(processor.processBotResponse(query), null);
        }
        return new BotReply(STALE_BUTTON_TEXT, null);
    }

    private BotReply handleNumber(long chatId, ChatContext context, String text) {
        var number = parseNumber(text);
        if (number.isEmpty()) {
            return new BotReply(INVALID_NUMBER_TEXT + promptFor(context), null);
        }
        return moveTo(chatId, new ChatContext(State.AWAITING_SOURCE,
                context.conversionType(), number.get().toPlainString(), null, null));
    }

    private Optional<BigDecimal> parseNumber(String text) {
        try {
            var value = new BigDecimal(text.trim().replace(',', '.'));
            boolean inRange = value.signum() >= 0 && value.compareTo(MAX_VALUE) < 0;
            return inRange ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private BotReply moveTo(long chatId, ChatContext next) {
        stateService.updateContext(chatId, next);
        return replyFor(next);
    }

    private BotReply replyFor(ChatContext context) {
        return new BotReply(promptFor(context), keyboardFactory.createFor(context));
    }

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