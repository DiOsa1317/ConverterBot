package ru.urfu.converterbot.keyboard.models;

/**
 * Строитель контекста чата {@link ChatContext}.
 * Позволяет пошагово задать поля состояния диалога без позиционных аргументов.
 */
public class ChatContextBuilder {
    private State state;
    private String conversionType;
    private String number;
    private String fromUnit;
    private String toUnit;

    /**
     * Устанавливает текущее состояние диалога.
     *
     * @param state состояние, например {@link State#IDLE}, {@link State#AWAITING_NUMBER} и т.д.
     * @return этот билдер для цепочки вызовов
     */
    public ChatContextBuilder state(State state) {
        this.state = state;
        return this;
    }

    /**
     * Устанавливает тип конвертации (код группы, например {@code CURRENCY}, {@code LENGTH} и т.д.).
     *
     * @param conversionType строковый код типа конвертации
     * @return этот билдер для цепочки вызовов
     */
    public ChatContextBuilder conversionType(String conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    /**
     * Устанавливает строковое представление числа для перевода.
     *
     * @param number числовое значение в строковом виде
     * @return этот билдер для цепочки вызовов
     */
    public ChatContextBuilder number(String number) {
        this.number = number;
        return this;
    }

    /**
     * Устанавливает код исходной единицы измерения.
     *
     * @param fromUnit код единицы, из которой переводим (например, {@code USD}, {@code METER})
     * @return этот билдер для цепочки вызовов
     */
    public ChatContextBuilder fromUnit(String fromUnit) {
        this.fromUnit = fromUnit;
        return this;
    }

    /**
     * Устанавливает код целевой единицы измерения.
     *
     * @param toUnit код единицы, в которую переводим (например, {@code EUR}, {@code KILOMETER})
     * @return этот билдер для цепочки вызовов
     */
    public ChatContextBuilder toUnit(String toUnit) {
        this.toUnit = toUnit;
        return this;
    }

    /**
     * Создаёт экземпляр {@link ChatContext} с заданными полями.
     *
     * @return новый контекст чата с текущими значениями полей билдера
     */
    public ChatContext build() {
        return new ChatContext(state, conversionType, number, fromUnit, toUnit);
    }
}
