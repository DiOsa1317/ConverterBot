package ru.urfu.converterbot.keyboard;

import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.KeyboardButton;
import ru.urfu.converterbot.keyboard.models.KeyboardRow;
import ru.urfu.converterbot.keyboard.models.KeyboardView;
import ru.urfu.converterbot.service.models.*;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public KeyboardView createMainMenu() {
        var buttons = List.of(
                new KeyboardButton("Валюты", "TYPE_CURRENCY"),
                new KeyboardButton("Длина", "TYPE_LENGTH"),
                new KeyboardButton("Вес", "TYPE_WEIGHT"),
                new KeyboardButton("Температура", "TYPE_TEMPERATURE")
        );
        return new KeyboardView(List.of(new KeyboardRow(buttons)));
    }

    public KeyboardView createSourceUnitKeyboard(String conversionType) {
        List<KeyboardButton> buttons = new ArrayList<>();
        for (QuantityType unit : unitsOf(conversionType)) {
            buttons.add(new KeyboardButton(unit.getCode(), "FROM:" + unit.getCode()));
        }
        return new KeyboardView(List.of(new KeyboardRow(buttons)));
    }

    public KeyboardView createTargetUnitKeyboard(String conversionType, String fromUnit) {
        List<KeyboardButton> buttons = new ArrayList<>();
        for (QuantityType unit : unitsOf(conversionType)) {
            if (unit.getCode().equals(fromUnit)) {
                continue;
            }
            buttons.add(new KeyboardButton(unit.getCode(), "FROM:" + unit.getCode()));
        }
        return new KeyboardView(List.of(new KeyboardRow(buttons)));
    }

    public KeyboardView createFor(ChatContext context) {
        return switch (context.state()) {
            case IDLE -> createMainMenu();
            case AWAITING_SOURCE -> createSourceUnitKeyboard(context.conversionType());
            case AWAITING_TARGET -> createTargetUnitKeyboard(context.conversionType(), context.fromUnit());
            default -> null;
        };
    }

    public List<QuantityType> unitsOf(String conversionType) {
        return switch (conversionType) {
            case "CURRENCY" -> List.of(CurrencyType.values());
            case "LENGTH" -> List.of(LengthType.values());
            case "WEIGHT" -> List.of(WeightType.values());
            case "TEMPERATURE" -> List.of(TemperatureType.values());
            default -> List.of();
        };
    }
}
