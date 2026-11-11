package ru.urfu.converterbot.keyboard.models;

public class ChatContextBuilder {
    private State state;
    private String conversionType;
    private String number;
    private String fromUnit;
    private String toUnit;

    public ChatContextBuilder state(State state) {
        this.state = state;
        return this;
    }

    public ChatContextBuilder conversionType(String conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    public ChatContextBuilder number(String number) {
        this.number = number;
        return this;
    }

    public ChatContextBuilder fromUnit(String fromUnit) {
        this.fromUnit = fromUnit;
        return this;
    }

    public ChatContextBuilder toUnit(String toUnit) {
        this.toUnit = toUnit;
        return this;
    }

    public ChatContext build() {
        return new ChatContext(state, conversionType, number, fromUnit, toUnit);
    }
}
