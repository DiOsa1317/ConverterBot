package ru.urfu.converterbot.keyboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.KeyboardButton;
import ru.urfu.converterbot.keyboard.models.KeyboardRow;
import ru.urfu.converterbot.keyboard.models.KeyboardView;
import ru.urfu.converterbot.service.models.*;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static final String TYPE_PREFIX = "TYPE:";

    public static final String FROM_PREFIX = "FROM:";

    public static final String TO_PREFIX = "TO:";

    /**
     * Создаёт главное меню с категориями конвертации.
     *
     * @return клавиатура главного меню
     */
    @NotNull
    public KeyboardView createMainMenu() {
        return toView(List.of(
                new KeyboardButton("Валюты", TYPE_PREFIX + "CURRENCY"),
                new KeyboardButton("Температура", TYPE_PREFIX + "TEMPERATURE"),
                new KeyboardButton("Вес", TYPE_PREFIX + "WEIGHT"),
                new KeyboardButton("Длина", TYPE_PREFIX + "LENGTH")), true);
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
            buttons.add(new KeyboardButton(unit.getCode(), FROM_PREFIX + unit.getCode()));
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
                buttons.add(new KeyboardButton(unit.getCode(), TO_PREFIX + unit.getCode()));
            }
        }
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

    /* Создает клавиатуру только с кнопкой меню для стартового сообщения */
    public KeyboardView toMenu() {
        var rows = new ArrayList<KeyboardRow>();
        rows.add(new KeyboardRow(List.of(
                new KeyboardButton("Меню", "MENU")
        )));

        return new KeyboardView(rows);
    }

    private KeyboardView toView(List<KeyboardButton> buttons, Boolean isMain) {
        var rows = new ArrayList<KeyboardRow>();

        for (int i = 0; i < buttons.size(); i += 2) {
            rows.add(new KeyboardRow(
                    buttons.subList(i, Math.min(i + 2, buttons.size()))
            ));
        }

        if (!isMain) {
            rows.add(new KeyboardRow(List.of(
                    new KeyboardButton("Меню", "MENU")
            )));
        }

        return new KeyboardView(rows);
    }
}
