package ru.urfu.converterbot.keyboard.service;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.*;

import ru.urfu.converterbot.service.BotResponseProcessor;
import ru.urfu.converterbot.service.CustomConversionRepository;

import java.sql.SQLException;

/** Обработчик нажатий на клавиатуру пользователем*/
public class ActionProcessor implements MessageProcessor {

    private static final String STALE_BUTTON_TEXT =
            "Эта кнопка устарела. Введите /menu, чтобы начать заново.";
    private static final String CUSTOM_CONVERSION_MENU_TEXT = "Прямые конверсии кастомных единиц";
    private static final String CUSTOM_STEP_ONE = "Шаг 1. Назовите исходную единицу (только латиница и _):";
    private static final String EMPTY_LIST_TEXT = "Конверсий пока нет.";
    private static final String LIST_TITLE_TEXT = "Ваши конверсии:";
    private static final String ERROR_TEXT = "Произошла ошибка. Попробуйте позже";
    private static final String WRONG_PAIR_TEXT = "Некорректная пара";

    private final KeyboardFactory keyboardFactory;
    private final ConversionStateService stateService;
    private final BotResponseProcessor processor;
    private final CustomConversionRepository customConversionRepository;

    /**
     * Создаёт сервис диалога.
     *
     * @param stateService хранилище состояний
     * @param keyboardFactory фабрика клавиатур
     * @param processor обработчик текстовых запросов
     */
    public ActionProcessor(@NotNull ConversionStateService stateService,
                           @NotNull KeyboardFactory keyboardFactory,
                           @NotNull BotResponseProcessor processor,
                           @NotNull CustomConversionRepository customConversionRepository) {
        this.stateService = stateService;
        this.processor = processor;
        this.keyboardFactory = keyboardFactory;
        this.customConversionRepository = customConversionRepository;
    }

    @Override
    public BotReply handle(long chatId, @Nullable String data) {
        if (data == null || data.isBlank()) {
            return new BotReply(STALE_BUTTON_TEXT);
        }

        var context = stateService.getChatContext(chatId);
        if (data.equals("MENU")) {
            stateService.reset(chatId);
            return replyFor(stateService.getChatContext(chatId));
        }
        if (data.startsWith(ButtonAction.TYPE + ":")) {
            String type = data.substring(ButtonAction.TYPE.toString().length() + 1);
            return moveTo(chatId, new ChatContextBuilder().state(State.AWAITING_NUMBER).conversionType(type).build());
        }
        if (data.startsWith(ButtonAction.FROM + ":")
                && context.state() == State.AWAITING_SOURCE) {
            var fromUnit = data.substring(ButtonAction.FROM.toString().length() + 1);
            return moveTo(chatId, new ChatContextBuilder()
                    .state(State.AWAITING_TARGET)
                    .conversionType(context.conversionType())
                    .number(context.number())
                    .fromUnit(fromUnit)
                    .build());
        }
        if (data.startsWith(ButtonAction.TO + ":")
                && context.state() == State.AWAITING_TARGET) {
            var toUnit = data.substring(ButtonAction.TO.toString().length() + 1);
            stateService.reset(chatId);
            var query = context.number() + " " + context.fromUnit() + " to " + toUnit;
            return new BotReply(processor.processBotResponse(query));
        }
        if (data.equals(ButtonAction.CUSTOM_CONVERSIONS.toString())) {
            stateService.reset(chatId);
            return new BotReply(CUSTOM_CONVERSION_MENU_TEXT, keyboardFactory.createCustomConversionMenu());
        }
        if (data.equals(ButtonAction.ADD_CUSTOM_CONV.toString())) {
            stateService.updateContext(chatId, new ChatContext(State.AWAITING_CUSTOM_FROM, null,
                    null, null, null));
            return new BotReply(CUSTOM_STEP_ONE);
        }
        if (data.equals(ButtonAction.LIST_CUSTOM_CONV.toString())) {
            try {
                var list = customConversionRepository.findAllOutgoing(chatId);
                if (list.isEmpty()) {
                    return new BotReply(EMPTY_LIST_TEXT, keyboardFactory.createCustomConversionMenu());
                }
                return new BotReply(LIST_TITLE_TEXT, keyboardFactory.createCustomConversionList(list));
            } catch (SQLException e) {
                return new BotReply(ERROR_TEXT, keyboardFactory.createCustomConversionMenu());
            }
        }
        if (data.startsWith(ButtonAction.DELETE_CUSTOM_CONV + ":")) {
            var pair = data.substring(ButtonAction.DELETE_CUSTOM_CONV.toString().length() + 1);
            var parts = pair.split(":");
            if (parts.length != 2) {
                return new BotReply(WRONG_PAIR_TEXT, keyboardFactory.createCustomConversionMenu());
            }
            try {
                customConversionRepository.deleteByChatIdAndFromAndTo(chatId, parts[0], parts[1]);
                return new BotReply("Удалено: " + parts[0] + " -> " + parts[1],
                        keyboardFactory.createCustomConversionMenu());
            } catch (SQLException e) {
                return new BotReply(ERROR_TEXT, keyboardFactory.createCustomConversionMenu());
            }
        }
        return new BotReply(STALE_BUTTON_TEXT);
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
            case AWAITING_CUSTOM_FROM -> "Введите название исходной единицы (латиница и _):";
            case AWAITING_CUSTOM_TO -> "Введите название целевой единицы (латиница и _):";
            case AWAITING_CUSTOM_RATE -> "Введите курс конверсии (число больше 0):";
            case CUSTOM_CONVERSIONS_MENU -> CUSTOM_CONVERSION_MENU_TEXT;
        };
    }
}
