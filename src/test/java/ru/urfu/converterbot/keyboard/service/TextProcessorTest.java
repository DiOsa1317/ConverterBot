package ru.urfu.converterbot.keyboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotMessageFormatter;
import ru.urfu.converterbot.service.CustomConversionRepository;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты обработчика текстовых сообщений {@link TextProcessor}.
 */
class TextProcessorTest {
    /** Сервис состояний диалога. */
    private ConversionStateService stateService;
    /** Обработчик текста, создаваемый перед каждым тестом. */
    private TextProcessor processor;

    /**
     * Создаёт сервисы перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        stateService = new ConversionStateService();
        var repo = new CustomConversionRepository(null) {
            @Override
            public void save(CustomConversion conversion) {}
            @Override
            public Optional<CustomConversion> findByChatidFromAndAndTo(long chatId, String from, String to) {
                return Optional.empty();
            }
            @Override
            public List<CustomConversion> findAllOutgoing(long chatId) {
                return List.of();
            }
            @Override
            public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) {
                return false;
            }
            @Override
            public List<String> findAllUnitNames(long chatId) {
                return List.of();
            }
            @Override
            public boolean existsByChatIdAndFromAndTo(long chatId, String from, String to) {
                return false;
            }
        };
        processor = new TextProcessor(stateService, new KeyboardFactory(),
                msg -> "converted:" + msg, new BotMessageFormatter(), repo);
    }

    /**
     * Проверяет ответ на команду /start.
     */
    @Test
    @DisplayName("/start возвращает приветствие и главное меню")
    void start() {
        var reply = processor.handle(1L, "/start");
        assertTrue(reply.text().contains("Приветствую"));
        assertNotNull(reply.keyboard());
    }

    /**
     * Проверяет ответ на команду /help.
     */
    @Test
    @DisplayName("/help возвращает справку")
    void help() {
        var reply = processor.handle(1L, "/help");
        assertTrue(reply.text().contains("Формат запроса"));
    }

    /**
     * Проверяет ответ на null в состоянии IDLE.
     */
    @Test
    @DisplayName("null в IDLE -> подсказка меню")
    void nullIdle() {
        var reply = processor.handle(1L, null);
        assertTrue(reply.text().contains("Используйте кнопки меню"));
        assertNotNull(reply.keyboard());
    }

    /**
     * Проверяет обработку валидного числа.
     */
    @Test
    @DisplayName("Число в состоянии AWAITING_NUMBER -> переход к выбору единицы")
    void number() {
        stateService.updateContext(1L, new ChatContext(State.AWAITING_NUMBER,
                "CURRENCY", null, null, null));
        var reply = processor.handle(1L, "100");
        assertTrue(reply.text().contains("стартовую"));
        assertNotNull(reply.keyboard());
    }

    /**
     * Проверяет обработку невалидного числа.
     */
    @Test
    @DisplayName("Невалидное число -> сообщение об ошибке и null-клавиатура")
    void badNumber() {
        stateService.updateContext(1L, new ChatContext(State.AWAITING_NUMBER,
                "CURRENCY", null, null, null));
        var reply = processor.handle(1L, "abc");
        assertTrue(reply.text().contains("Неверный формат числа"));
        assertNull(reply.keyboard());
    }
}
