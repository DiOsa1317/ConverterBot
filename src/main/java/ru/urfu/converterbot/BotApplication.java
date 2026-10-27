package ru.urfu.converterbot;

import org.jetbrains.annotations.NotNull;
import ru.urfu.converterbot.bots.TelegramBot;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.renderers.TelegramKeyboardRenderer;
import ru.urfu.converterbot.keyboard.service.ConversionStateService;
import ru.urfu.converterbot.service.*;
import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.converters.models.LengthData;
import ru.urfu.converterbot.service.converters.models.TemperatureData;
import ru.urfu.converterbot.service.converters.models.WeightData;

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

            var botService = getBotService();

            var keyBotToken = config.getProperty(configuration.keyBotToken());
            var keyBotUsername = config.getProperty(configuration.keyBotUsername());
            var stateService = new ConversionStateService();
            var keyboardFactory = new KeyboardFactory();
            var renderer = new TelegramKeyboardRenderer();
            var telegramBot = new TelegramBot(keyBotToken, keyBotUsername,
                    botService, stateService, keyboardFactory, renderer);
            telegramBot.start();
        } catch (Exception e) {
            logger.error("Ошибка при запуске", e);
            System.exit(1);
        }
    }

    private static @NotNull BotService getBotService() {
        var parser = new ClientInputParser();
        var formatter = new BotMessageFormatter();
        var lengthConverter = new LengthConverter(new LengthData());
        var weightConverter = new WeightConverter(new WeightData());
        var currencyData = new CurrencyData();
        var currencyConverter = new CurrencyConverter(currencyData);
        CurrencyRateService currencyRateService = new CurrencyRateService(currencyData);
        currencyRateService.start();
        var temperatureConverter = new TemperatureConverter(new TemperatureData());

        return new BotService(
                currencyConverter,
                lengthConverter,
                weightConverter,
                temperatureConverter,
                parser,
                formatter);
    }
}