package ru.urfu.converterbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
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

/**
 * Сервис получения и обновления курсов валют.
 */
public class CurrencyRateService {

    private static final String URL =
            "https://www.cbr.ru/scripts/XML_daily.asp";

    private static final String DISALLOW_DOCTYPE =
            "http://apache.org/xml/features/disallow-doctype-decl";

    private static final long REFRESH_PERIOD_HOURS = 6;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final CurrencyData currencyData;
    private final HttpClient httpClient;

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
            var rates = parseRates(fetchRatesXml());
            var updatedAt = LocalDateTime.now();
            currencyData.update(rates, updatedAt);
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
     * Получает данные о курсах валют.
     *
     * @return данные о курсах валют
     */
    private String fetchRatesXml() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("ЦБ РФ вернул HTTP " + response.statusCode());
        }
        return response.body();
    }

    /**
     * Преобразует данные о курсах валют в набор курсов.
     *
     * @param xml исходные данные о курсах валют
     * @return курсы валют
     */
     Map<CurrencyType, BigDecimal> parseRates(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE, true);
        var document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        var rates = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
        rates.put(CurrencyType.RUB, BigDecimal.ONE);
        var currencies = document.getElementsByTagName("Valute");
        for (var i = 0; i < currencies.getLength(); i++) {
            var currency = (Element) currencies.item(i);
            var code = text(currency, "CharCode");

            CurrencyType type;
            try {
                type = CurrencyType.valueOf(code);
            } catch (IllegalArgumentException unsupported) {
                continue;
            }
            var nominal = new BigDecimal(text(currency, "Nominal"));
            var value = new BigDecimal(text(currency, "Value").replace(',', '.'));
            rates.put(type, value.divide(nominal, 10, RoundingMode.HALF_UP));
        }

        for (var type : CurrencyType.values()) {
            if (!rates.containsKey(type)) {
                throw new IllegalStateException("В ответе ЦБ отсутствует валюта " + type);
            }
        }
        return rates;
    }

    /**
     * Возвращает текст элемента с указанным именем.
     *
     * @param parent родительский элемент
     * @param tag    имя элемента
     * @return текст элемента
     */
    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent();
    }
}