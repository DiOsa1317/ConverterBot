package ru.urfu.converterbot.keyboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.urfu.converterbot.keyboard.models.ButtonAction;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.KeyboardButton;
import ru.urfu.converterbot.keyboard.models.KeyboardRow;
import ru.urfu.converterbot.keyboard.models.KeyboardView;
import ru.urfu.converterbot.service.models.*;

import java.util.ArrayList;
import java.util.List;

/** Класс для создания клавиатур */
public class KeyboardFactory {
    
    /**
     * Создаёт главное меню с категориями конвертации.
     *
     * @return клавиатура главного меню
     */
    @NotNull
    public KeyboardView createMainMenu() {
        return toView(List.of(
                new KeyboardButton("Валюты", ButtonAction.TYPE, "CURRENCY"),
                new KeyboardButton("Температура", ButtonAction.TYPE, "TEMPERATURE"),
                new KeyboardButton("Вес", ButtonAction.TYPE, "WEIGHT"),
                new KeyboardButton("Длина", ButtonAction.TYPE, "LENGTH"),
                new KeyboardButton("Мои конверсии", ButtonAction.CUSTOM_CONVERSIONS, "")
        ), true);
    }

    /**
     * Создаёт клавиатуру выбора исходной единицы измерения.
     *
     * @param conversionType категория конвертации
     * @return клавиатура выбора исходной единицы и кнопка меню
     */
    @NotNull
    public KeyboardView createSourceUnitKeyboard(@NotNull String conversionType) {
        var buttons = new ArrayList<KeyboardButton>();
        for (QuantityType unit : unitsOf(conversionType)) {
            buttons.add(new KeyboardButton(unit.getCode(), ButtonAction.FROM, unit.getCode()));
        }
        return toView(buttons, false);
    }

    /**
     * Создаёт клавиатуру выбора целевой единицы измерения.
     *
     * <p>Исходная единица исключается из списка вариантов.</p>
     *
     * @param conversionType категория конвертации
     * @param fromUnit код исходной единицы
     * @return клавиатура выбора целевой единицы и кнопка меню
     */
    @NotNull
    public KeyboardView createTargetUnitKeyboard(
                @NotNull String conversionType, 
                @NotNull String fromUnit) {
        var buttons = new ArrayList<KeyboardButton>();
        for (QuantityType unit : unitsOf(conversionType)) {
            if (!unit.getCode().equals(fromUnit)) {
                buttons.add(new KeyboardButton(unit.getCode(), ButtonAction.TO, unit.getCode()));
            }
        }
        return toView(buttons, false);
    }

    @NotNull
    public KeyboardView createCustomConversionMenu() {
        return toView(List.of(
                new KeyboardButton("Добавить конверсию", ButtonAction.ADD_CUSTOM_CONV, ""),
                new KeyboardButton("Мои конверсии", ButtonAction.LIST_CUSTOM_CONV, ""),
                new KeyboardButton("В главное меню", ButtonAction.MENU, "")
                ), false);
    }

    @NotNull
    public KeyboardView createCustomConversionList(List<CustomConversion> conversions) {
        var buttons = new ArrayList<KeyboardButton>();
        for (var conversion : conversions) {
            buttons.add(new KeyboardButton(conversion.fromUnit() + " -> " + conversion.toUnit(),
                    ButtonAction.DELETE_CUSTOM_CONV, conversion.fromUnit() + ":" + conversion.toUnit()));
        }
        buttons.add(new KeyboardButton("В главное меню", ButtonAction.MENU, ""));
        return toView(buttons, false);
    }

    /**
     * Создаёт клавиатуру, соответствующую текущему состоянию диалога.
     *
     * @param context текущее состояние диалога
     * @return подходящая клавиатура или {@code null}, если она не нужна
     */
    @Nullable
    public KeyboardView createFor(ChatContext context) {
        return switch (context.state()) {
            case IDLE -> createMainMenu();
            case AWAITING_SOURCE -> createSourceUnitKeyboard(context.conversionType());
            case AWAITING_TARGET -> createTargetUnitKeyboard(context.conversionType(), context.fromUnit());
            default -> null;
        };
    }

    /**
     * Возвращает единицы измерения выбранной категории.
     *
     * @param conversionType категория: CURRENCY, LENGTH, WEIGHT или TEMPERATURE
     * @return список единиц измерения; пустой список для неизвестной категории
     */
    @NotNull
    public List<QuantityType> unitsOf(@NotNull String conversionType) {
        return switch (conversionType) {
            case "CURRENCY" -> List.of(CurrencyType.values());
            case "LENGTH" -> List.of(LengthType.values());
            case "WEIGHT" -> List.of(WeightType.values());
            case "TEMPERATURE" -> List.of(TemperatureType.values());
            default -> List.of();
        };
    }

    /**
     * Задает клавиатурное отображение набора кнопок
     *
     * @param buttons - кнопки клавиатуры
     * @param isMain - является ли клавиатура главным меню
     * @return вид клавиатуры
     */
    private KeyboardView toView(List<KeyboardButton> buttons, Boolean isMain) {
        var rows = new ArrayList<KeyboardRow>();

        for (int i = 0; i < buttons.size(); i += 2) {
            rows.add(new KeyboardRow(
                    buttons.subList(i, Math.min(i + 2, buttons.size()))
            ));
        }

        if (!isMain) {
            rows.add(new KeyboardRow(List.of(
                    new KeyboardButton("Меню", ButtonAction.MENU, "")
            )));
        }

        return new KeyboardView(rows);
    }
}
