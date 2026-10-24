package ru.urfu.converterbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Полные тесты для CurrencyRateService.
 */
class CurrencyRateServiceTest {

    private CurrencyData currencyData;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockHttpResponse;
    private CurrencyRateService service;

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

    @BeforeEach
    void setUp() throws IOException, InterruptedException { // Исправлено: добавлено InterruptedException
        currencyData = mock(CurrencyData.class);
        mockHttpClient = mock(HttpClient.class);
        mockHttpResponse = mock(HttpResponse.class);

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(SAMPLE_XML);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        service = new CurrencyRateService(currencyData, mockHttpClient);
    }

    @Test
    @DisplayName("Успешно обновляет курсы и сохраняет их в хранилище")
    void testUpdateRatesSuccess() {
        boolean result = service.updateRates();

        assertTrue(result, "Обновление должно пройти успешно");

        ArgumentCaptor<Map<CurrencyType, BigDecimal>> ratesCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(currencyData).update(ratesCaptor.capture(), dateCaptor.capture());

        Map<CurrencyType, BigDecimal> savedRates = ratesCaptor.getValue();

        assertNotNull(savedRates);
        assertEquals(BigDecimal.ONE, savedRates.get(CurrencyType.RUB), "Курс RUB должен быть 1");
        assertEquals(new BigDecimal("92.5000000000"), savedRates.get(CurrencyType.USD));
        assertEquals(new BigDecimal("100.2500000000"), savedRates.get(CurrencyType.EUR));
        assertEquals(new BigDecimal("12.8000000000"), savedRates.get(CurrencyType.CNY));
        assertEquals(new BigDecimal("0.1850000000"), savedRates.get(CurrencyType.KZT));
    }

    @Test
    @DisplayName("Возвращает false при ошибке HTTP (статус не 200)")
    void testUpdateRatesHttpError() {
        when(mockHttpResponse.statusCode()).thenReturn(500);

        boolean result = service.updateRates();

        assertFalse(result);
        verify(currencyData, never()).update(any(), any());
    }

    @Test
    @DisplayName("Возвращает false при ошибке парсинга XML")
    void testUpdateRatesParseError() {
        when(mockHttpResponse.body()).thenReturn("<broken>xml");

        boolean result = service.updateRates();

        assertFalse(result);
        verify(currencyData, never()).update(any(), any());
    }

    @Test
    @DisplayName("Не падает с исключением при сетевой ошибке")
    void testUpdateRatesNetworkException() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException("Network error"));

        assertDoesNotThrow(() -> {
            boolean result = service.updateRates();
            assertFalse(result);
        });
    }

    @Test
    @DisplayName("Корректно парсит полный XML ответ ЦБ")
    void testParseRatesFullXml() throws Exception {
        Map<CurrencyType, BigDecimal> rates = service.parseRates(SAMPLE_XML);

        assertNotNull(rates);
        assertEquals(5, rates.size());

        assertEquals(BigDecimal.ONE, rates.get(CurrencyType.RUB));
        assertEquals(new BigDecimal("92.5000000000"), rates.get(CurrencyType.USD));
        assertEquals(new BigDecimal("0.1850000000"), rates.get(CurrencyType.KZT));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если в XML нет обязательной валюты")
    void testParseRatesMissingCurrency() {
        String incompleteXml = """
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
        String xmlWithBtc = SAMPLE_XML.replace("</ValCurs>",
                "<Valute><CharCode>BTC</CharCode><Nominal>1</Nominal><Value>1000</Value></Valute></ValCurs>");

        assertDoesNotThrow(() -> service.parseRates(xmlWithBtc));
    }

    @Test
    @DisplayName("start запускает первичное обновление")
    void testStartTriggersInitialUpdate() {
        service.start();
        verify(currencyData, atLeastOnce()).update(any(), any());
        service.stop();
    }

    @Test
    @DisplayName("stop безопасно работает, если start не вызывался")
    void testStopWithoutStart() {
        CurrencyRateService newService = new CurrencyRateService(currencyData, mockHttpClient);
        assertDoesNotThrow(newService::stop);
    }
}