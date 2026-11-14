package ru.urfu.converterbot.keyboard.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.BotReply;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.ChatContextBuilder;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotResponseProcessor;
import ru.urfu.converterbot.service.CustomConversionRepository;
import ru.urfu.converterbot.service.MessageFormatter;
import ru.urfu.converterbot.service.models.CustomConversion;

/**
 * Обработчик текстовых сообщений пользователя
 */
public class TextProcessor implements MessageProcessor {

    private static final String MENU_COMMAND = "/menu";
    private static final String START_COMMAND = "/start";
    private static final String HELP_COMMAND = "/help";

    private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000");

    private static final String INVALID_NUMBER_TEXT = "Неверный формат числа. ";
    private static final String NUMBER_EXAMPLE_TEXT = "Введите число. Например: 100";
    private static final String MENU_HINT_TEXT = "Используйте кнопки меню для выбора типа перевода.";

    private static final String WRONG_CUSTOM_CONV_NAME =
            "Название должно содержать только латинские буквы и _. Попробуйте ещё раз.";
    private static final String WRONG_MESSAGE_FORMAT_TEXT = "Ошибка формата. Введите число, например, 250 или 50.5.";
    private static final String COURSE_SHOULD_BE_GREATER_THAN_ZERO = "Курс должен быть больше 0.";
    private static final String ERROR_MESSAGE = "Произошла ошибка. Пожалуйста, попробуйте позже.";

    private final ConversionStateService stateService;

    private final KeyboardFactory keyboardFactory;

    private final BotResponseProcessor processor;

    private final MessageFormatter formatter;

    private final CustomConversionRepository customConversionRepository;

    /**
     * Создаёт сервис диалога.
     *
     * @param stateService    хранилище состояний
     * @param keyboardFactory фабрика клавиатур
     * @param processor       готовит ответ бота
     */
    public TextProcessor(@NotNull ConversionStateService stateService,
                         @NotNull KeyboardFactory keyboardFactory,
                         @NotNull BotResponseProcessor processor,
                         @NotNull MessageFormatter messageFormatter,
                         @NotNull CustomConversionRepository customConversionRepository) {
        this.stateService = stateService;
        this.keyboardFactory = keyboardFactory;
        this.processor = processor;
        this.formatter = messageFormatter;
        this.customConversionRepository = customConversionRepository;
    }

    @Override
    public BotReply handle(long chatId, @Nullable String text) {
        var context = stateService.getChatContext(chatId);
        if (text == null || text.isBlank()) {
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
            return handleNumber(chatId, context, text);
        }

        if (context.state() == State.AWAITING_CUSTOM_FROM) {
            return handleCustomFrom(chatId, text);
        }

        if (context.state() == State.AWAITING_CUSTOM_TO) {
            return handleCustomTo(chatId, context, text);
        }

        if (context.state() == State.AWAITING_CUSTOM_RATE) {
            return handleCustomRate(chatId, context, text);
        }

        return new BotReply(
                processor.processBotResponse(chatId, text) + MENU_HINT_TEXT,
                keyboardFactory.createMainMenu()
        );
    }

    /**
     * Обрабатывает сообщение-число
     *
     * @param chatId  - id чата
     * @param context - контекст чата
     * @param text    - сообщение пользователя
     * @return ответ бота
     *
     */
    private BotReply handleNumber(long chatId, ChatContext context, String text) {
        var number = parseNumber(text);
        if (number.isEmpty()) {
            return new BotReply(INVALID_NUMBER_TEXT + promptFor(context), null);
        }
        return moveTo(chatId, new ChatContextBuilder().state(State.AWAITING_SOURCE)
                .conversionType(context.conversionType())
                .number(number.get().toPlainString())
                .build());
    }

    private BotReply handleCustomFrom(long chatId, String text) {
        var from = text.trim();
        if (!from.matches("^[a-zA-Z_]+$")) {
            return new BotReply(WRONG_CUSTOM_CONV_NAME);
        }
        stateService.updateContext(chatId,
                new ChatContext(State.AWAITING_CUSTOM_TO, null, from, null, null));
        return new BotReply("Отлично. Теперь укаэжите целевую единицу (например, calorie).");
    }

    private BotReply handleCustomTo(long chatId, ChatContext context, String text) {
        var to = text.trim();
        if (!to.matches("^[a-zA-Z_]+$")) {
            return new BotReply(WRONG_CUSTOM_CONV_NAME);
        }
        var from = context.number();
        stateService.updateContext(chatId,
                new ChatContext(State.AWAITING_CUSTOM_RATE, null, from, to, null));
        return new BotReply("Укажите курс: сколько " + to + " содержится в 1 " + from + "?");
    }

    private BotReply handleCustomRate(long chatId, ChatContext context, String text) {
        var rateStr = text.trim().replace(',', '.');
        BigDecimal rate;
        try {
            rate = new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            return new BotReply(WRONG_MESSAGE_FORMAT_TEXT);
        }
        if (rate.signum() <= 0) {
            return new BotReply(COURSE_SHOULD_BE_GREATER_THAN_ZERO);
        }
        var from = context.number();
        var to = context.fromUnit();
        var conversion = new CustomConversion(from, to, rate, chatId);
        try {
            customConversionRepository.save(conversion);
        } catch (SQLException e) {
            return new BotReply(ERROR_MESSAGE);
        }
        stateService.reset(chatId);
        return new BotReply("Готово! 1 " + from + " = " + rate + " " + to, keyboardFactory.createMainMenu());
    }

    /**
     * Превращает число-строку в число
     *
     * @param text - строка, приводимая к числу
     * @return число после парсинга или Optional.empty(), если ввод не подходит под критерии
     *
     */
    private Optional<BigDecimal> parseNumber(String text) {
        try {
            var value = new BigDecimal(text.trim().replace(',', '.'));
            var inRange = value.signum() >= 0 && value.compareTo(MAX_VALUE) < 0;
            return inRange ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    /**
     * Возвращает ответ для пользователя и перемещает на следующий этап
     *
     * @param chatId - id чата
     * @param next   - следующий контекст чата
     * @return ответ бота
     *
     */
    private BotReply moveTo(long chatId, ChatContext next) {
        stateService.updateContext(chatId, next);
        return replyFor(next);
    }

    /**
     * Подбирает ответ для пользователя в зависимости от контекста
     *
     * @param context - следующий контекст чата
     * @return ответ бота
     *
     */
    private BotReply replyFor(ChatContext context) {
        return new BotReply(promptFor(context), keyboardFactory.createFor(context));
    }

    /**
     * Подбирает строковую часть ответа для пользователя в зависимости от контекста
     *
     * @param context - следующий контекст чата
     * @return строковая часть ответа
     *
     */
    private String promptFor(ChatContext context) {
        return switch (context.state()) {
            case IDLE -> "Выберите тип перевода:";
            case AWAITING_NUMBER -> "Введите число x, где 0 <= x < 10^9. "
                    + "Дробные числа вводите через точку.";
            case AWAITING_SOURCE -> "Выберите стартовую единицу:";
            case AWAITING_TARGET -> "Выберите конечную единицу:";
            case AWAITING_CUSTOM_FROM -> "Введите название исходной единицы (латиница и _):";
            case AWAITING_CUSTOM_TO -> "Введите название целевой единицы (латиница и _):";
            case AWAITING_CUSTOM_RATE -> "Введите курс конверсии (число больше 0):";
            case CUSTOM_CONVERSIONS_MENU -> "Меню кастомных конверсий";
        };
    }
}
