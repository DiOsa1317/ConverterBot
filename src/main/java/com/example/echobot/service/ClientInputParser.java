package com.example.echobot.service;


import java.util.AbstractMap;
import java.util.Map;

public class ClientInputParser {
    public <T extends Enum<T>> Map.Entry<T, T> parse(String message, Class<T> enumClass) {
        String[] parts = message.split(" ");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Сообщение должно выглядеть как «[число] [единица 1] to [единица 2]»");
        }

        T first = Enum.valueOf(enumClass, parts[0].toUpperCase());
        T second = Enum.valueOf(enumClass, parts[1].toUpperCase());

        return new AbstractMap.SimpleImmutableEntry<>(first, second);
    }

}

