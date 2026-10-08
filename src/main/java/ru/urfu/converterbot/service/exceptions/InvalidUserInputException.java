package ru.urfu.converterbot.service.exceptions;

/**
 * Исключение, сигнализирующее о том, что сообщение пользователя не соответствует
 * ожидаемому формату ввода (до какой-либо попытки конвертации).
 */
public class InvalidUserInputException extends RuntimeException {

    /** Исходное сообщение пользователя, из-за которого возникла ошибка. */
    private final String userInput;

    /**
     * Создаёт исключение с указанием исходного сообщения пользователя и причины ошибки.
     *
     * @param userInput исходное сообщение, присланное пользователем
     * @param reason текст, объясняющий, почему сообщение не подходит
     */
    public InvalidUserInputException(String userInput, String reason) {
        super("Некорректный ввод: \"" + userInput + "\". " + reason);
        this.userInput = userInput;
    }

    /**
     * Возвращает исходное сообщение пользователя, вызвавшее ошибку.
     *
     * @return исходный текст пользователя
     */
    public String getUserInput() {
        return userInput;
    }
}