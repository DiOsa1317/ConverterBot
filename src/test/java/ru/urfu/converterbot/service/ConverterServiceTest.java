package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ru.urfu.converterbot.service.exceptions.ConversionException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ConverterService: форматирование результата перевода,
 * справка по командам групп и отказ переводить между разными группами величин.
 */
public class ConverterServiceTest {

    /** Диспетчер перевода, создаваемый заново перед каждым тестом. */
    private ConverterService converterService;

    /**
     * Создаёт реестр со всеми конвертерами и диспетчер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        var registry = new ConverterRegistry(List.of(
                new CurrencyConverter(),
                new LengthConverter(),
                new WeightConverter(),
                new TemperatureConverter()
        ));
        converterService = new ConverterService(registry);
    }

    /**
     * Проверяет, что результат переводится с четырьмя знаками после запятой
     * через шаблон вывода.
     */
    @Test
    @DisplayName("Форматирует результат четырьмя знаками после запятой")
    void shouldFormatResult() {
        assertEquals("10.0000 USD = 843.4140 RUB",
                converterService.convert("10.000 USD to RUB"));
    }

    /**
     * Проверяет, что по каждой команде группы возвращается непустая справка.
     *
     * @param command команда справки для одной из зарегистрированных групп
     */
    @ParameterizedTest(name = "Команда: '{0}'")
    @ValueSource(strings = {"/currency", "/length", "/weight", "/temperature"})
    @DisplayName("Возвращает справку по каждой группе величин")
    void shouldReturnHelpByCommand(String command) {
        assertFalse(converterService.getHelp(command).isBlank());
    }

    /**
     * Проверяет, что перевод между единицами разных групп отклоняется
     * с пояснением для пользователя.
     */
    @Test
    @DisplayName("Отказывает в переводе между разными группами величин")
    void shouldRejectConversionBetweenDifferentGroups() {
        var exception = assertThrows(ConversionException.class,
                () -> converterService.convert("10 KILOMETER to KILOGRAM"));
        assertFalse(exception.getMessage().isBlank());
    }

    /**
     * Проверяет, что любая ошибка ввода превращается в исключение
     * с сообщением на русском языке, которое увидит пользователь.
     *
     * @param message входное сообщение с ошибкой формата или неизвестной единицей
     */
    @ParameterizedTest(name = "Мусор: '{0}'")
    @ValueSource(strings = {"привет", "10 USD to", "10 BTC to USD", "abc USD to EUR"})
    @DisplayName("Отвечает понятной ошибкой на некорректный ввод")
    void shouldFailWithRussianMessageBadInput(String message) {
        var exception = assertThrows(ConversionException.class,
                () -> converterService.convert(message));
        assertTrue(exception.getMessage().matches(".*[а-яА-Я].*"),
                "Сообщение должно быть на русском: " + exception.getMessage());
    }

}