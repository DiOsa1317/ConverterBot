package ru.urfu.converterbot.bots;

import ru.urfu.converterbot.service.BotResponseProcessor;
import ru.urfu.converterbot.service.ConversionException;
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

    /**
     * Обрабатывает входящие обновления от Telegram API.
     * Реализует логику бота: конвертирует величины.
     *
     * @param update объект обновления, содержащий данные о событии
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.equalsIgnoreCase("/help") || messageText.equalsIgnoreCase("/start")) {
            sendHelpMessage(chatId);
            return;
        }

        String responseText;
        try {
            responseText = botResponseProcessor.processBotResponse(messageText);
        } catch (ConversionException exception) {
            responseText = exception.getMessage();
        } catch (Exception exception) {
            logger.error("Непредвиденная ошибка при обработке сообщения: ", exception);
            responseText = "Произошла непредвиденная ошибка. Попробуйте ещё раз.";
        }

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
     * Отправляет пользователю справочное сообщение с инструкцией по использованию бота.
     *
     * @param chatId идентификатор чата
     */
    private void sendHelpMessage(long chatId) {
        String helpText = """
                Этот бот конвертирует валюты и физические величины.
                
                Формат запроса: {число} {единица1} to {единица2}
                Например: 100 USD to EUR
                Или: 10 KM to MI
                
                Доступные валюты: /currency
                Доступные величины:
                1) /length
                2) /weight
                3) /temperature
                """;

        var message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(helpText);

        try {
            execute(message);
        } catch (Exception exception) {
            logger.error("Ошибка при отправке справки: ", exception);
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

    /**
     * Возвращает имя пользователя (username) данного бота.
     * Требуется родительским классом для идентификации при подключении к API.
     *
     * @return username бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Возвращает токен авторизации данного бота.
     * Требуется родительским классом для аутентификации запросов к Telegram API.
     *
     * @return токен бота
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}