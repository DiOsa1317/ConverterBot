package ru.urfu.converterbot.keyboard.service;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.BotReply;
import ru.urfu.converterbot.keyboard.models.ButtonAction;
import ru.urfu.converterbot.keyboard.models.ChatContext;

import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotResponseProcessor;

public class ActionProcessor implements MessageProcessor {

    private static final String STALE_BUTTON_TEXT =
            "Эта кнопка устарела. Введите /menu, чтобы начать заново.";

    private final KeyboardFactory keyboardFactory;
    private final ConversionStateService stateService;
    private final BotResponseProcessor processor;

    /**
     * Создаёт сервис диалога.
     *
     * @param stateService хранилище состояний
     * @param keyboardFactory фабрика клавиатур
     * @param processor обработчик текстовых запросов
     */
    public ActionProcessor(@NotNull ConversionStateService stateService,
                           @NotNull KeyboardFactory keyboardFactory,
                           @NotNull BotResponseProcessor processor) {
        this.stateService = stateService;
        this.processor = processor;
        this.keyboardFactory = keyboardFactory;
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
        if (data.startsWith(ButtonAction.TYPE.toString() + ":")) {
            String type = data.substring(ButtonAction.TYPE.toString().length() + 1);
            return moveTo(chatId, new ChatContext(State.AWAITING_NUMBER, type, null, null, null));
        }
        if (data.startsWith(ButtonAction.FROM.toString() + ":")
                && context.state() == State.AWAITING_SOURCE) {
            var fromUnit = data.substring(ButtonAction.FROM.toString().length() + 1);
            return moveTo(chatId, new ChatContext(State.AWAITING_TARGET,
                    context.conversionType(), context.number(), fromUnit, null));
        }
        if (data.startsWith(ButtonAction.TO.toString() + ":")
                && context.state() == State.AWAITING_TARGET) {
            var toUnit = data.substring(ButtonAction.TO.toString().length() + 1);
            stateService.reset(chatId);
            var query = context.number() + " " + context.fromUnit() + " to " + toUnit;
            return new BotReply(processor.processBotResponse(query));
        }
        return new BotReply(STALE_BUTTON_TEXT);
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
