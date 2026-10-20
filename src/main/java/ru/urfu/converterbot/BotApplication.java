package ru.urfu.converterbot;

import ru.urfu.converterbot.bots.TelegramBot;
import ru.urfu.converterbot.service.*;
import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;

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
            var configuration = new BotConfiguration("config.properties",
                    "bot.token", "bot.username");
            var config = configuration.loadConfiguration();
            logger.info("Конфигурация успешно загружена");

            var parser = new ClientInputParser();
            var formatter = new BotMessageFormatter();
            var lengthConverter = new LengthConverter();
            var weightConverter = new WeightConverter();
            var currencyConverter = new CurrencyConverter();
            var temperatureConverter = new TemperatureConverter();
            var converterService = new BotService(
                                            currencyConverter,
                                            lengthConverter,
                                            weightConverter,
                                            temperatureConverter,
                                            parser,
                                            formatter);

            var keyBotToken = config.getProperty(configuration.keyBotToken());
            var keyBotUsername = config.getProperty(configuration.keyBotUsername());
            var telegramBot = new TelegramBot(keyBotToken, keyBotUsername, converterService);
            telegramBot.start();
        } catch (Exception e) {
            logger.error("Ошибка при запуске", e);
            System.exit(1);
        }
    }
}