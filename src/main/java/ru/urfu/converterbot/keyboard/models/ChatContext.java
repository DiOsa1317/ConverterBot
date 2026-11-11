package ru.urfu.converterbot.keyboard.models;

/** Контекст чата
 * @param state - текущее состояние диалога
 * @param conversionType - тип конвертации
 * @param number - значение для перевода
 * @param fromUnit - из какой единицы переводим
 * @param toUnit - в какую единицу переводим
 * */
public record ChatContext(
        State state,
        String conversionType,
        String number,
        String fromUnit,
        String toUnit
) {
    public static ChatContextBuilder builder() {
        return new ChatContextBuilder();
    }
}
