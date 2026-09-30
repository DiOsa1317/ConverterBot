package com.example.echobot;

import com.example.echobot.bots.TelegramBot;
import com.example.echobot.service.CurrencyConverter;
import com.example.echobot.service.EchoService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Точка входа в приложение TelegramBot.
 * Отвечает за загрузку конфигурации из локального файла,
 * создание экземпляра бота и его регистрацию в Telegram API
 * через механизм Long Polling.
 */
public class BotApplication {

    /**
     * Логгер для записи событий запуска приложения.
     */
    private static final Logger logger = LoggerFactory.getLogger(BotApplication.class);

    /**
     * Главный метод приложения. Запускает процесс инициализации и регистрации бота.
     *
     * @param args аргументы командной строки (в текущей реализации не используются)
     */
    static void main(String[] args) {
        try {
            var configuration = new Configuration("config.properties",
                    "bot.token", "bot.username");
            var config = configuration.loadConfiguration();
            logger.info("Конфигурация успешно загружена");

            var currencyConverter = new CurrencyConverter();
            var keyBotToken = config.getProperty(configuration.keyBotToken());
            var keyBotUsername = config.getProperty(configuration.keyBotUsername());
            var telegramBot = new TelegramBot(keyBotToken, keyBotUsername, currencyConverter);
            telegramBot.start();
        } catch (Exception e) {
            logger.error("Ошибка при запуске", e);
            System.exit(1);
        }
    }
}