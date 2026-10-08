package ru.urfu.converterbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @param configFileName Имя файла с локальной конфигурацией (токен и имя бота).
 * @param keyBotToken    Ключ для получения токена авторизации из свойств конфигурации.
 * @param keyBotUsername Ключ для получения имени пользователя бота (username) из свойств конфигурации.
 */
public record Configuration(String configFileName, String keyBotToken, String keyBotUsername) {

    /**
     * Загружает свойства из файла config.properties, расположенного в корне проекта.
     * Использует try-with-resources для автоматического закрытия потока ввода.
     *
     * @return объект Properties с загруженными данными конфигурации
     * @throws IOException если файл не найден, недоступен для чтения или поврежден
     */
    public Properties loadConfiguration() throws IOException, IllegalStateException {
        var properties = new Properties();
        try (InputStream input = new FileInputStream(configFileName)) {
            properties.load(input);
           validateConfig(properties);
        }
        return properties;
    }

    /**
     * Проверяет наличие обязательных параметров в конфигурации.
     *
     * @param config загруженные свойства конфигурации
     * @throws IllegalStateException если отсутствует токен или имя бота
     */
    private void validateConfig(Properties config) {
        if (config.getProperty(keyBotToken) == null ||
                config.getProperty(keyBotUsername) == null) {
            throw new IllegalStateException(
                    "В файле " + configFileName + " отсутствуют токен (bot.token) или имя бота (bot.username)"
            );
        }
    }
}
