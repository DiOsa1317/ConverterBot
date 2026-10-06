package com.example.echobot.service;

/**
 * Ошибка разбора или перевода запроса. Наследует IllegalArgumentException,
 * потому что всегда вызвана некорректным вводом пользователя, а текст сообщения
 * написан по-русски и показывает пользователю как есть.
 */
public class ConversionException extends IllegalArgumentException {

    /**
     * Создаёт исключение с текстом для пользователя.
     *
     * @param message сообщение на русском языке, которое увидит пользователь
     */
    public ConversionException(String message) {
        super(message);
    }
}
