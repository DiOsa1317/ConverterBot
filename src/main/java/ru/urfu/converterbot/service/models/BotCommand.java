package ru.urfu.converterbot.service.models;

public enum BotCommand {
    CURRENCY("/currency"),
    TEMPERATURE("/temperature"),
    WEIGHT("/weight"),
    LENGTH("/length");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
