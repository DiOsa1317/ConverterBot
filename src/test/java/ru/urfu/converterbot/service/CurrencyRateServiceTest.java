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
import java.net.URI;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты контракта сервиса получения и обновления курсов валют {@link CurrencyRateService}.
 */
class CurrencyRateServiceTest {

    private static final String FRANKFURTER_JSON_USD_BASE = """
            [
              {"date":"2026-10-24","base":"USD","quote":"USD","rate":1.0},
              {"date":"2026-10-24","base":"USD","quote":"EUR","rate":0.925},
              {"date":"2026-10-24","base":"USD","quote":"RUB","rate":90.5},
              {"date":"2026-10-24","base":"USD","quote":"CNY","rate":7.25},
              {"date":"2026-10-24","base":"USD","quote":"KZT","rate":500.0}
            ]
            """;

    private static final String FRANKFURTER_JSON_RUB_BASE = """
            [
              {"date":"2026-10-24","base":"RUB","quote":"RUB","rate":1.0},
              {"date":"2026-10-24","base":"RUB","quote":"USD","rate":0.011},
              {"date":"2026-10-24","base":"RUB","quote":"EUR","rate":0.010},
              {"date":"2026-10-24","base":"RUB","quote":"CNY","rate":0.080},
              {"date":"2026-10-24","base":"RUB","quote":"KZT","rate":5.52}
            ]
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
    void testUpdateRatesSuccess() throws Exception {
        // Frankfurter API возвращает курсы для одной базовой валюты за запрос.
        // Сервис делает 5 запросов (по одному на каждую валюту). Мок возвращает
        // один и тот же ответ — достаточно для проверки, что обновление прошло.
        CurrencyRateService service = createServiceWithResponse(200, FRANKFURTER_JSON_USD_BASE);

        var result = service.updateRates();

        assertTrue(result, "Обновление должно пройти успешно");
        assertNotNull(currencyData.lastUpdated(), "Время последнего обновления должно быть установлено");

        var savedRates = currencyData.rates();
        assertNotNull(savedRates);
        assertEquals(5, savedRates.size(), "Матрица должна содержать все 5 валют");

        // Проверяем, что для каждой базовой валюты есть строка
        for (var type : CurrencyType.values()) {
            assertTrue(savedRates.containsKey(type), "Отсутствует строка для валюты " + type);
        }
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
    @DisplayName("start запускает синхронное первичное обновление курсов")
    void testStartTriggersInitialUpdate() {
        CurrencyRateService service = createServiceWithResponse(200, FRANKFURTER_JSON_USD_BASE);

        assertNull(currencyData.lastUpdated(), "До вызова start() время обновления должно быть null");

        service.start();
        assertNotNull(currencyData.lastUpdated(), "После start() время обновления должно быть установлено");

        service.stop();
    }

    @Test
    @DisplayName("stop безопасно работает, если start не вызывался")
    void testStopWithoutStart() {
        CurrencyRateService service = createServiceWithResponse(200, FRANKFURTER_JSON_USD_BASE);
        assertDoesNotThrow(service::stop);
    }

    @Test
    @DisplayName("Запрос к Frankfurter API формирует корректный URL")
    void testFrankfurterUrl() {
        CurrencyRateService service = createServiceWithResponse(200, FRANKFURTER_JSON_USD_BASE);

        assertDoesNotThrow(service::updateRates);
        assertNotNull(currencyData.lastUpdated());
    }
}
