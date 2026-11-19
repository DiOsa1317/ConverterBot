package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.TemperatureType;
import ru.urfu.converterbot.service.models.fakeClasses.FakeCustomConversionRepository;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.models.CustomQuantityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Тесты для ClientInputParser.
 */
public class ClientInputParserTest {
    /** Экземпляр парсера, создаваемый заново перед каждым тестом. */
    private ClientInputParser clientInputParser;

    /**
     * Создаёт новый парсер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        clientInputParser = new ClientInputParser(new FakeCustomConversionRepository());
    }

    /**
     * Проверяет, что валюты корректно извлекаются из входного сообщения.
     * @throws InvalidUserInputException 
     */
    @Test
    @DisplayName("Правильно извлек валюты из текста пользователя")
    void shouldParseValidMessagesWithCurrency() throws InvalidUserInputException {
        var result = clientInputParser
                .parse( "567 USD to CNY", 1);
        assertEquals(CurrencyType.USD, result.from());
        assertEquals(CurrencyType.CNY, result.to());
    }

    /**
     * Проверяет, что регистр букв в единицах измерения не важен.
     *
     * @param input входное сообщение, записанное в смешанном регистре
     * @throws InvalidUserInputException 
     */
    @ParameterizedTest(name = "Вход: '{0}'")
    @ValueSource(strings = {
            "234 EuR to kzT",
            "763 eur to kzt",
    })
    @DisplayName("Считывает названия величин вне зависимости от регистра")
    void shouldParseValidMessagesWithLowercase(String input) throws InvalidUserInputException {
        var result = clientInputParser.parse(input, 1);
        assertEquals(CurrencyType.EUR, result.from());
        assertEquals(CurrencyType.KZT, result.to());
    }


    /**
     * Проверяет, что при неверном количестве слов бросается исключение.
     *
     * @param input входное сообщение со структурой, отличной от формата парсера
     */
    @ParameterizedTest(name = "Неверная длина: '{0}'")
    @ValueSource(strings = {
            "10 USD to",
            "10 USD to EUR now",
            "USD",
            ""
    })
    @DisplayName("Выбрасывает исключение при неверном количестве слов")
    void shouldThrowExceptionOnWrongLength(String input) {
        assertThrows(InvalidUserInputException.class, () -> clientInputParser.parse(input, 1));
    }

    /**
     * Проверяет, что неизвестная валюта отклоняется парсером.
     */
    @Test
    @DisplayName("Выбрасывает исключение для неизвестной валюты")
    void shouldThrowExceptionOnInvalidCurrency() {
        String input = "10 BTC to USD";

        assertThrows(InvalidUserInputException.class, () -> clientInputParser.parse(input, 1));
    }

    /**
     * Проверяет, что разделителем между единицами может быть только слово {@code to}.
     */
    @Test
    @DisplayName("Выбрасывает исключение, если разделитель не равен to")
    void shouldThrowExceptionOnWrongSeparator() {
        assertThrows(InvalidUserInputException.class,
                () -> clientInputParser.parse("10 USD XX EUR", 1));
    }

    /**
     * Проверяет, что единицы физических величин распознаются
     * наравне с валютами.
     * @throws InvalidUserInputException 
     */
    @Test
    @DisplayName("Разбирает единицы физических величин")
    void shouldParsePhysicalUnits() throws InvalidUserInputException {
        var result = clientInputParser.parse("20 CELSIUS to FAHRENHEIT", 1);
        assertEquals(TemperatureType.CELSIUS, result.from());
        assertEquals(TemperatureType.FAHRENHEIT, result.to());
    }

    /**
     * Проверяет, что парсер распознает пользовательскую единицу измерения.
     */
    @Test
    @DisplayName("Распознаёт пользовательскую единицу измерения")
    void shouldParseCustomUnit() {
        var repository = new FakeCustomConversionRepository();

        repository.saveOrUpdate(new CustomConversion(
                "kk",
                "mm",
                new BigDecimal("2"),
                1L
        ));

        var parser = new ClientInputParser(repository);

        var result = parser.parse("5 kk to mm", 1L);

        assertEquals(new BigDecimal("5"), result.value());
        assertInstanceOf(CustomQuantityType.class, result.from());
        assertInstanceOf(CustomQuantityType.class, result.to());
    }
}
