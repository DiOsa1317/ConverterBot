package ru.urfu.converterbot.bots;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.KeyboardView;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.keyboard.renderers.TelegramKeyboardRenderer;
import ru.urfu.converterbot.keyboard.service.ConversionStateService;
import ru.urfu.converterbot.service.BotResponseProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


/**
 * Основной класс бота для Telegram.
 * Реализует прием сообщений пользователя
 * и возвращение ему ответа
 */
public class TelegramBot extends TelegramLongPollingBot {

    /**
     * Токен авторизации бота, полученный от BotFather.
     */
    private final String botToken;

    /**
     * Имя пользователя (username) бота без символа @.
     */
    private final String botUsername;

    /**
     * Сервис для бизнес-логики обработки сообщений.
     */
    private final BotResponseProcessor botResponseProcessor;

    /**
     * Логгер для записи событий при работе бота
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final ConversionStateService conversionStateService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramKeyboardRenderer telegramKeyboardRenderer;

    /**
     * Создает новый экземпляр бота.
     *
     * @param token       токен авторизации бота
     * @param name        имя пользователя бота (username)
     * @param botResponseProcessor сервис для подготовки ответа
     */
    public TelegramBot(String token, String name, BotResponseProcessor botResponseProcessor,
                       ConversionStateService conversionStateService,
                       KeyboardFactory keyboardFactory,
                       TelegramKeyboardRenderer telegramKeyboardRenderer) {
        super(token);
        this.botToken = token;
        this.botUsername = name;
        this.botResponseProcessor = botResponseProcessor;
        this.conversionStateService = conversionStateService;
        this.keyboardFactory = keyboardFactory;
        this.telegramKeyboardRenderer = telegramKeyboardRenderer;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage()) {
            return;
        }

        String messageText = update.getMessage().hasText() ? update.getMessage().getText() : null;
        long chatId = update.getMessage().getChatId();
        var context = conversionStateService.getChatContext(chatId);

        if (context.state() == State.AWAITING_NUMBER) {
            var nextContext = new ChatContext(State.AWAITING_SOURCE, context.conversionType(),
                    messageText, null, null);
            conversionStateService.updateContext(chatId,nextContext);
            var keyboard = keyboardFactory.createFor(nextContext);
            sendWithKeyboard(chatId, promptTextFor(nextContext), keyboard);
            return;
        }

        String responseText = botResponseProcessor.processBotResponse(messageText);

        sendText(chatId, responseText);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        try {
            var answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            answer.setText("Выполняю...");
            execute(answer);
        } catch (Exception e) {
            logger.error("Ошибка при ответе на callback: ", e);
        }
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getMessage().getChatId();
        var context = conversionStateService.getChatContext(chatId);
        ChatContext next;

        if (data.startsWith("IDLE")) {
            next = new ChatContext(State.AWAITING_NUMBER, null, null, null, null);
        } else if (data.startsWith("TYPE_")) {
            var type = data.substring("TYPE_".length());
            next = new ChatContext(State.AWAITING_SOURCE, type, context.number(), null, null);
        } else if (data.startsWith("FROM:")) {
            var fromUnit = data.substring("FROM:".length());
            next = new ChatContext(State.AWAITING_TARGET, context.conversionType(),
                    context.number(), fromUnit, null);
        } else if (data.startsWith("TO:")) {
            var toUnit = data.substring("TO:".length());
            next = new ChatContext(State.RESULT, context.conversionType(), context.number(),
                    context.fromUnit(), toUnit);
        } else {
            next = context;
        }

        conversionStateService.updateContext(chatId, next);

        if (data.startsWith("TO:")) {
            botResponseProcessor.processBotResponse(
                    String.format("$d $s to $s",
                    context.number(),
                    context.fromUnit(),
                    context.toUnit()
            ));
            conversionStateService.reset(chatId);
            return;
        }

        var keyboard = keyboardFactory.createFor(next);
        sendWithKeyboard(chatId, promptTextFor(next), keyboard);
    }

    private void sendWithKeyboard(long chatId, String text, KeyboardView keyboard) {
        var markup = (keyboard == null) ? null : telegramKeyboardRenderer.render(keyboard);
        var message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения: ", e);
        }
    }

    private void sendText(long chatId, String text) {
        var message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(chatId));
        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения: ", e);
        }
    }

    private String promptTextFor(ChatContext chatContext) {
        return switch (chatContext.state()) {
            case IDLE -> "Выберите тип перевода:";
            case AWAITING_NUMBER -> "Введите число (0 <= x < 10^9). Дробные числа - через точку.";
            case AWAITING_SOURCE -> "Выберите стартовую единицу:";
            case AWAITING_TARGET -> "Выберите конечную единицу:";
            case RESULT -> "Готово";
        };
    }

    /**
     * Инициализирует и регистрирует Telegram-бота в API.
     * @throws TelegramApiException если произошла ошибка при регистрации бота
     */
    public void start()
            throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
        logger.info("Telegram-бот '" + botUsername + "' успешно подключен.");
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}