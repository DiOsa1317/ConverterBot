package ru.urfu.converterbot.bots;

import ru.urfu.converterbot.keyboard.models.BotReply;
import ru.urfu.converterbot.keyboard.renderers.TelegramKeyboardRenderer;
import ru.urfu.converterbot.keyboard.service.KeyboardDialogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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
     * Имя пользователя (username) бота без символа @.
     */
    private final String botUsername;

    /**
     * Логгер для записи событий при работе бота
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final KeyboardDialogService dialogService;

    private final TelegramKeyboardRenderer keyboardRenderer;

    /**
     * Создает новый экземпляр бота.
     *
     * @param token       токен авторизации бота
     * @param name        имя пользователя бота (username)
     * @param botResponseProcessor сервис для подготовки ответа
     */
    public TelegramBot(String token, String name,
                   KeyboardDialogService dialogService,
                   TelegramKeyboardRenderer keyboardRenderer) {
        super(token);
        this.botUsername = name;
        this.dialogService = dialogService;
        this.keyboardRenderer = keyboardRenderer;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            acknowledge(callback.getId());
            long chatId = callback.getMessage().getChatId();
            send(chatId, dialogService.handleAction(chatId, callback.getData()));
            return;
        }
        if (!update.hasMessage()) {
            return;
        }
        var message = update.getMessage();
        String text = message.hasText() ? message.getText() : null;
        long chatId = message.getChatId();
        send(chatId, dialogService.handleText(chatId, text));
    }

    private void acknowledge(String callbackId) {
        var answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        try {
            execute(answer);
        } catch (TelegramApiException exception) {
            logger.error("Ошибка при ответе на callback: ", exception);
        }
    }

    private void send(long chatId, BotReply reply) {
        var message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(reply.text());
        if (reply.keyboard() != null) {
            message.setReplyMarkup(keyboardRenderer.render(reply.keyboard()));
        }
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            logger.error("Ошибка при отправке сообщения: ", exception);
        }
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