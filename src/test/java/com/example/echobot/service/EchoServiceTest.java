package com.example.echobot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для проверки бизнес-логики EchoService.
 */
public class EchoServiceTest {

    /** Экземпляр эхо-сервиса, создаваемый заново перед каждым тестом. */
    private EchoService echoService;

    /**
     * Создаёт эхо-сервис перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        echoService = new EchoService();
    }

    /**
     * Проверяет, что сервис возвращает входной текст без изменений.
     *
     * @param input входное сообщение пользователя
     */
    @ParameterizedTest(name = "Вход: '{0}'")
    @ValueSource(strings = {
            "Привет",
            "Cтрока с пробелами",
            "До переноса строки\nПеред табуляцией\tПосле неё"
    })
    @DisplayName("processBotResponse возвращает идентичный входной текст")
    void shouldReturnExactSameText(String input) {
        String result = echoService.processBotResponse(input);
        assertEquals(input, result, "Метод должен вернуть ТОЧНО тот же текст");
    }

    /**
     * Проверяет, что на пустую строку возвращается подсказка вместо эха.
     */
    @Test
    @DisplayName("processBotResponse обрабатывает пустую строку")
    void shouldHandleEmptyString() {
        String result = echoService.processBotResponse("");
        assertEquals("Сообщение не должно быть пустым", result);
    }

    /**
     * Проверяет, что строка из пробелов и управляющих символов
     * считается пустой.
     */
    @Test
    @DisplayName("processBotResponse обрабатывает строку из пробелов")
    void shouldHandleBlankString() {
        String result = echoService.processBotResponse("   \t\n  ");
        assertEquals("Сообщение не должно быть пустым", result);
    }
}