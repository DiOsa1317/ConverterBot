package ru.urfu.converterbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.models.CurrencyData;
import ru.urfu.converterbot.service.models.CurrencyType;
import ru.urfu.converterbot.service.models.QuantityType;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Сервис получения и обновления курсов валют с сайта Центрального банка РФ.
 */
public class CurrencyRateService {

    private static final String URL = 
        "https://www.cbr.ru/scripts/XML_daily.asp";

    private final CurrencyData currencyData;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private ScheduledExecutorService scheduler;
    
    private final CurrencyConverter currencyConverter;

    private final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

    public CurrencyRateService(CurrencyData currencyData, CurrencyConverter currencyConverter) {
        this.currencyData = currencyData;
        this.currencyConverter = currencyConverter;
    }

    /**
     * Получает актуальные курсы валют с сайта ЦБ РФ, разбирает XML-ответ
     * и обновляет данные конвертера.
     * 
     * Курсы приводятся к стоимости одной единицы валюты в рублях.
     * Если запрос завершается ошибкой или в ответе отсутствует
     * поддерживаемая валюта, существующие курсы не заменяются.
     */
    public void updateRates() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "ЦБ РФ вернул HTTP " + response.statusCode()
                );
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            var document = factory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(response.body()))
            );

            Map<QuantityType, BigDecimal> newRates = new HashMap<>();
            newRates.put(CurrencyType.RUB, BigDecimal.ONE);

            NodeList currencies = document.getElementsByTagName("Valute");

            for (int i = 0; i < currencies.getLength(); i++) {
                Element currency = (Element) currencies.item(i);

                String code = currency
                        .getElementsByTagName("CharCode")
                        .item(0)
                        .getTextContent();

                CurrencyType type;

                try {
                    type = CurrencyType.valueOf(code);
                } catch (IllegalArgumentException exception) {
                    // Валюта из ответа ЦБ не поддерживается ботом.
                    continue;
                }

                BigDecimal nominal = new BigDecimal(
                        currency.getElementsByTagName("Nominal")
                                .item(0).getTextContent()
                );

                BigDecimal value = new BigDecimal(
                        currency.getElementsByTagName("Value")
                                .item(0).getTextContent()
                                .replace(',', '.')
                );

                BigDecimal rate = value.divide(
                        nominal,
                        10,
                        RoundingMode.HALF_UP
                );

                newRates.put(type, rate);
            }

            for (CurrencyType type : CurrencyType.values()) {
                if (!newRates.containsKey(type)) {
                    throw new IllegalStateException(
                            "В ответе ЦБ отсутствует валюта " + type
                    );
                }
            }

            currencyData.update(newRates);
            currencyConverter.lastUpdated = LocalDateTime.now();

            logger.info("Курсы валют обновлены: {}", currencyConverter.lastUpdated);

        } catch (Exception exception) {
            logger.error("Не удалось обновить курсы валют", exception);
        }
    }

    /**
     * Выполняет первоначальное обновление курсов и запускает их
     * периодическое обновление каждые шесть часов.
     */
    public void start() {
        updateRates();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                this::updateRates,
                6,
                6,
                TimeUnit.HOURS
        );
    }
}