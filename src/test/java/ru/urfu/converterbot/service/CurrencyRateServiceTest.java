package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.fakeClasses.FakeHttpClient;
import ru.urfu.converterbot.service.models.fakeClasses.FakeHttpResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты контракта сервиса получения и обновления курсов валют {@link CurrencyRateService}.
 */
class CurrencyRateServiceTest {

    private static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="windows-1251"?>
            <ValCurs Date="24.10.2026" name="Foreign Currency Market">
                <Valute ID="R01235">
                    <CharCode>USD</CharCode>
                    <Nominal>1</Nominal>
                    <Value>92,5000</Value>
                </Valute>
                <Valute ID="R01239">
                    <CharCode>EUR</CharCode>
                    <Nominal>1</Nominal>
                    <Value>100,2500</Value>
                </Valute>
                <Valute ID="R01375">
                    <CharCode>CNY</CharCode>
                    <Nominal>1</Nominal>
                    <Value>12,8000</Value>
                </Valute>
                <Valute ID="R01350">
                    <CharCode>KZT</CharCode>
                    <Nominal>100</Nominal>
                    <Value>18,5000</Value>
                </Valute>
            </ValCurs>
            """;

    private CurrencyData currencyData;

    @BeforeEach
    void setUp() {
        currencyData = new CurrencyData();
    }

    private CurrencyRateService createServiceWithResponse(int statusCode, String body) {
        var response = new FakeHttpResponse(statusCode, body);
        return new CurrencyRateService(currencyData, new FakeHttpClient(response));
    }

    private CurrencyRateService createServiceWithException(IOException exception) {
        return new CurrencyRateService(currencyData, new FakeHttpClient(exception));
    }

    @Test
    @DisplayName("Успешно обновляет курсы и сохраняет их в реальном хранилище")
    void testUpdateRatesSuccess() {
        CurrencyRateService service = createServiceWithResponse(200, SAMPLE_XML);

        var result = service.updateRates();

        assertTrue(result, "Обновление должно пройти успешно");
        assertNotNull(currencyData.lastUpdated(), "Время последнего обновления должно быть установлено");

        var savedRates = currencyData.rates();
        assertNotNull(savedRates);

        assertBigDecimalEquals("1", savedRates.get(CurrencyType.RUB));
        assertBigDecimalEquals("92.5", savedRates.get(CurrencyType.USD));
        assertBigDecimalEquals("100.25", savedRates.get(CurrencyType.EUR));
        assertBigDecimalEquals("12.8", savedRates.get(CurrencyType.CNY));
        assertBigDecimalEquals("0.185", savedRates.get(CurrencyType.KZT));
    }

    @Test
    @DisplayName("Возвращает false при ошибке HTTP и не меняет состояние хранилища")
    void testUpdateRatesHttpError() {
        CurrencyRateService service = createServiceWithResponse(500, "Internal Server Error");

        var result = service.updateRates();

        assertFalse(result);
        assertNull(currencyData.lastUpdated(), "Время обновления не должно измениться при ошибке");
    }

    @Test
    @DisplayName("Возвращает false при ошибке парсинга XML и не меняет состояние хранилища")
    void testUpdateRatesParseError() {
        CurrencyRateService service = createServiceWithResponse(200, "<broken>xml");

        var result = service.updateRates();

        assertFalse(result);
        assertNull(currencyData.lastUpdated(), "Время обновления не должно измениться при ошибке парсинга");
    }

    @Test
    @DisplayName("Не падает с исключением при сетевой ошибке")
    void testUpdateRatesNetworkException() {
        CurrencyRateService service = createServiceWithException(new IOException("Network error"));

        assertDoesNotThrow(() -> {
            var result = service.updateRates();
            assertFalse(result);
        });
        assertNull(currencyData.lastUpdated(), "Время обновления не должно измениться при сетевой ошибке");
    }

    @Test
    @DisplayName("Корректно парсит полный XML ответ ЦБ")
    void testParseRatesFullXml() throws Exception {
        CurrencyRateService service = createServiceWithResponse(200, "");

        var rates = service.parseRates(SAMPLE_XML);

        assertNotNull(rates);
        assertEquals(5, rates.size());

        assertBigDecimalEquals("1", rates.get(CurrencyType.RUB));
        assertBigDecimalEquals("92.5", rates.get(CurrencyType.USD));
        assertBigDecimalEquals("0.185", rates.get(CurrencyType.KZT));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если в XML нет обязательной валюты")
    void testParseRatesMissingCurrency() {
        CurrencyRateService service = createServiceWithResponse(200, "");

        var incompleteXml = """
                <?xml version="1.0" encoding="windows-1251"?>
                <ValCurs>
                    <Valute><CharCode>USD</CharCode><Nominal>1</Nominal><Value>90,0000</Value></Valute>
                    <Valute><CharCode>CNY</CharCode><Nominal>1</Nominal><Value>12,0000</Value></Valute>
                    <Valute><CharCode>KZT</CharCode><Nominal>100</Nominal><Value>18,0000</Value></Valute>
                </ValCurs>
                """;

        assertThrows(IllegalStateException.class, () -> service.parseRates(incompleteXml));
    }

    @Test
    @DisplayName("Игнорирует неизвестные валюты")
    void testParseRatesIgnoresUnknown() {
        CurrencyRateService service = createServiceWithResponse(200, "");

        var xmlWithBtc = SAMPLE_XML.replace("</ValCurs>",
                "<Valute><CharCode>BTC</CharCode><Nominal>1</Nominal><Value>1000</Value></Valute></ValCurs>");

        assertDoesNotThrow(() -> service.parseRates(xmlWithBtc));
    }

    @Test
    @DisplayName("start запускает синхронное первичное обновление курсов")
    void testStartTriggersInitialUpdate() {
        CurrencyRateService service = createServiceWithResponse(200, SAMPLE_XML);

        assertNull(currencyData.lastUpdated(), "До вызова start() время обновления должно быть null");
        BigDecimal initialUsdRate = currencyData.rates().get(CurrencyType.USD);

        service.start();
        assertNotNull(currencyData.lastUpdated(), "После start() время обновления должно быть установлено");

        BigDecimal updatedUsdRate = currencyData.rates().get(CurrencyType.USD);
        assertBigDecimalEquals("92.5", updatedUsdRate);
        assertNotEquals(initialUsdRate, updatedUsdRate, "Курс USD должен измениться после start()");

        service.stop();
    }

    @Test
    @DisplayName("stop безопасно работает, если start не вызывался")
    void testStopWithoutStart() {
        CurrencyRateService service = createServiceWithResponse(200, SAMPLE_XML);
        assertDoesNotThrow(service::stop);
    }

    /**
     * Вспомогательный метод для корректного сравнения BigDecimal.
     * Использует compareTo, который считает равными 92.5 и 92.5000000000.
     */
    private void assertBigDecimalEquals(String expectedStr, BigDecimal actual) {
        BigDecimal expected = new BigDecimal(expectedStr);
        assertEquals(0, expected.compareTo(actual),
                "Курс не совпадает. Ожидалось: " + expected + ", получено: " + actual);
    }
}