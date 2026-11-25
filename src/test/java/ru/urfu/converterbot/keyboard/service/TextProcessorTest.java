package ru.urfu.converterbot.keyboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.urfu.converterbot.db.DatabaseConfig;
import ru.urfu.converterbot.keyboard.KeyboardFactory;
import ru.urfu.converterbot.keyboard.models.ChatContext;
import ru.urfu.converterbot.keyboard.models.State;
import ru.urfu.converterbot.service.BotMessageFormatter;
import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.models.fakeClasses.FakeConversionHistoryRepository;
import ru.urfu.converterbot.service.models.fakeClasses.FakeCustomConversionRepository;
import ru.urfu.converterbot.service.repository.ConversionHistoryRepositoryImp;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты обработчика текстовых сообщений {@link TextProcessor}.
 */
class TextProcessorTest {
    /**
     * Сервис состояний диалога.
     */
    private ConversionStateService stateService;
    /**
     * Обработчик текста, создаваемый перед каждым тестом.
     */
    private TextProcessor processor;

    /**
     * Создаёт сервисы перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        stateService = new ConversionStateService();
        processor = new TextProcessor(stateService, new KeyboardFactory(),
                (chatId, msg) -> "converted:" + msg, new BotMessageFormatter(),
                new FakeCustomConversionRepository(), new FakeConversionHistoryRepository());
    }

    /**
     * Проверяет ответ на команду /start.
     */
    @Test
    @DisplayName("/start возвращает приветствие и главное меню")
    void start() {
        var reply = processor.handle(1L, "/start");
        assertEquals("Приветствую вас! Этот бот конвертирует валюты и физические величины.\nЧтобы узнать подробнее, введите /help\nИспользуйте кнопки меню для выбора типа перевода.", reply.text());
        assertNotNull(reply.keyboard());
    }

    /**
     * Проверяет ответ на команду /help.
     */
    @Test
    @DisplayName("/help возвращает справку")
    void help() {
        var reply = processor.handle(1L, "/help");
        assertEquals("Формат запроса: {число} {единица1} to {единица2}\nНапример: 100 USD to EUR\nИли: 10 KM to MI\n\nДоступные валюты: /currency\nДоступные величины:\n1) /length\n2) /weight\n3) /temperature\nИспользуйте кнопки меню для выбора типа перевода.", reply.text());
    }

    /**
     * Проверяет ответ на null в состоянии IDLE.
     */
    @Test
    @DisplayName("null в IDLE -> подсказка меню")
    void nullIdle() {
        var reply = processor.handle(1L, null);
        assertEquals("Используйте кнопки меню для выбора типа перевода.", reply.text());
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
        assertEquals("Выберите стартовую единицу:", reply.text());
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
        assertEquals("Неверный формат числа. Введите число x, где 0 <= x < 10^9. Дробные числа вводите через точку.", reply.text());
        assertNull(reply.keyboard());
    }

    @Test
    @DisplayName("Конвертация по прямому кастомному курсу через кнопку")
    void customConvertNumber() {
        var repo = new FakeCustomConversionRepository() {
            @Override
            public Optional<CustomConversion> findByChatIdFromAndAndTo(long chatId, String from, String to) {
                if ("cookie".equals(from) && "calorie".equals(to)) {
                    return Optional.of(new CustomConversion("cookie", "calorie", new BigDecimal("300"), chatId));
                }
                return Optional.empty();
            }
        };
        var historyRepository = new ConversionHistoryRepositoryImp(new DatabaseConfig());
        var customProcessor = new TextProcessor(stateService, new KeyboardFactory(),
                (chatId, msg) -> "converted:" + msg, new BotMessageFormatter(), repo, historyRepository);
        stateService.updateContext(1L, new ChatContext(State.AWAITING_CUSTOM_CONVERT_NUMBER,
                null, "cookie", "calorie", null));
        var reply = customProcessor.handle(1L, "10");
        assertEquals("10 cookie = 3000.0000 calorie\nИспользуйте кнопки меню для выбора типа перевода.", reply.text());
        assertNotNull(reply.keyboard());
    }
}
