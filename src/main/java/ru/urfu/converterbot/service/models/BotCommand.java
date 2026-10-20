package ru.urfu.converterbot.service.models;

public enum BotCommand {
    CURRENCY("/currency", "Величины валют"),
    TEMPERATURE("/temperature", "Величины температур"),
    WEIGHT("/weight", "Величины веса"),
    LENGTH("/length", "Величины длины");

    private final String command;

    private final String title;

    BotCommand(String command, String title) {
        this.command = command;
        this.title = title;
    }

    /**
     * Возвращает название заголовка величин.
     *
     * @return название для показа пользователю
     */
    public String title() {
        return title;
    }

    /**
     * Проверяет, соответствует ли текст этой команде. Регистр не важен.
     *
     * @param text текст сообщения пользователя
     * @return {@code true}, если текст совпадает с командой
     */
    public boolean matches(String text) {
        return command.equalsIgnoreCase(text);
    }
}
