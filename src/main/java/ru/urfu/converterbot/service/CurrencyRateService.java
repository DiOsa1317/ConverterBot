package ru.urfu.converterbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис получения и обновления курсов валют через Frankfurter API.
 */
public class CurrencyRateService {

    private static final String URL = "https://api.frankfurter.app/latest";

    private static final long REFRESH_PERIOD_HOURS = 6;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final CurrencyData currencyData;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ScheduledExecutorService scheduler;

    private final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

    /**
     * Создаёт сервис обновления курсов валют.
     *
     * @param currencyData хранилище курсов
     */
    public CurrencyRateService(CurrencyData currencyData) {
        this.currencyData = currencyData;
        httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    /**
     * Создаёт сервис обновления курсов валют.
     *
     * @param currencyData хранилище курсов
     * @param httpClient клиент, запрашивающий данные
     */
    public CurrencyRateService(CurrencyData currencyData, HttpClient httpClient) {
        this.currencyData = currencyData;
        this.httpClient = httpClient;
    }

    /**
     * Обновляет курсы валют.
     *
     * @return {@code true}, если обновление выполнено успешно; иначе {@code false}
     */
    public boolean updateRates() {
        try {
            var matrix = new EnumMap<CurrencyType, Map<CurrencyType, BigDecimal>>(CurrencyType.class);
            for (var base : CurrencyType.values()) {
                var row = fetchRatesForBase(base);
                matrix.put(base, row);
            }
            var updatedAt = LocalDateTime.now();
            currencyData.update(matrix, updatedAt);
            logger.info("Курсы валют обновлены: {}", updatedAt);
            return true;
        } catch (Exception exception) {
            logger.error("Не удалось обновить курсы валют, используются прежние", exception);
            return false;
        }
    }

    /**
     * Запускает обновление курсов валют.
     */
    public void start() {
        updateRates();

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, "currency-rate-updater");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(
                this::updateRates,
                REFRESH_PERIOD_HOURS,
                REFRESH_PERIOD_HOURS,
                TimeUnit.HOURS
        );
    }

    /**
     * Останавливает обновление курсов валют.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Загружает курсы для базовой валюты из Frankfurter API.
     *
     * @param base базовая валюта
     * @return курсы валют относительно базовой
     */
    private Map<CurrencyType, BigDecimal> fetchRatesForBase(CurrencyType base) throws Exception {
        var targets = Stream.of(CurrencyType.values())
                .filter(t -> t != base)
                .map(Enum::name)
                .collect(Collectors.joining(","));
        var uri = URI.create(URL + "?from=" + base.name() + "&to=" + targets);
        var request = HttpRequest.newBuilder().uri(uri).timeout(REQUEST_TIMEOUT).GET().build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Frankfurter API вернул HTTP " + response.statusCode());
        }
        var tree = objectMapper.readTree(response.body());
        var ratesNode = tree.get("rates");
        var row = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
        row.put(base, BigDecimal.ONE);
        for (var type : CurrencyType.values()) {
            if (type == base) continue;
            var value = ratesNode.get(type.name()).decimalValue();
            row.put(type, value.setScale(10, RoundingMode.HALF_UP));
        }
        return row;
    }
}
