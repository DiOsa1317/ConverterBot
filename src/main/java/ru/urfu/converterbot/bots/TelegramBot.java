package ru.urfu.converterbot.bots;

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
 * Наследует TelegramLongPollingBot и реализует прием сообщений пользователя и возвращение ему ответа
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

    /**
     * Создает новый экземпляр бота.
     *
     * @param token       токен авторизации бота
     * @param name        имя пользователя бота (username)
     * @param botResponseProcessor сервис для подготовки ответа
     */
    public TelegramBot(String token, String name, BotResponseProcessor botResponseProcessor) {
        this.botToken = token;
        this.botUsername = name;
        this.botResponseProcessor = botResponseProcessor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        String messageText = update.getMessage().hasText() ? update.getMessage().getText() : null;
        long chatId = update.getMessage().getChatId();

        String responseText = botResponseProcessor.processBotResponse(messageText);

        var message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(responseText);

        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения: ", e);
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
        System.out.println("Telegram-бот '" + botUsername + "' успешно подключен.");
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}