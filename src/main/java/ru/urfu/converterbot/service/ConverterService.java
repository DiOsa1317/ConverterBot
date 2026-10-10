package ru.urfu.converterbot.service;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.urfu.converterbot.service.exceptions.ConversionException;

/**
 * Диспетчер перевода величин. Принимает текстовое сообщение пользователя,
 * определяет группу величин через реестр и возвращает готовый ответ для чата.
 * Реализует интерфейс {@link BotResponseProcessor}.
 */
public class ConverterService implements BotResponseProcessor {

    /** Шаблон строки с результатом перевода. */
    private static final String RESULT_TEMPLATE = "%.4f %s = %.4f %s";

    /** Текст общей справки по командам бота. */
    private static final String HELP_TEXT = """
        Этот бот конвертирует валюты и физические величины.
        
        Формат запроса: {число} {единица1} to {единица2}
        Например: 100 USD to EUR
        Или: 10 KM to MI
        
        Доступные валюты: /currency
        Доступные величины:
        1) /length
        2) /weight
        3) /temperature
        """;
    
    private static final String START_TEXT = """
    Приветствую вас! Этот бот конвертирует валюты и физические величины.
    Чтобы узнать подробнее, введите /help
    """;
    
    /** Текст ответа при непредвиденной ошибке обработки сообщения. */
    private static final String UNEXPECTED_ERROR_TEXT = "Произошла непредвиденная ошибка. Попробуйте ещё раз.";

    /** Логгер для записи непредвиденных ошибок обработки сообщений. */
    private final Logger logger = LoggerFactory.getLogger(ConverterService.class);

    /** Реестр конвертеров, по которому определяется группа величин. */
    private final ConverterRegistry registry;

    /** Парсер ввода пользователя: превращает текст в запрос на перевод. */
    private final Parser clientInputParser;

    /**
     * Создаёт диспетчер с собственным парсером ввода.
     *
     * @param registry реестр конвертеров, в котором ищется группа величин
     */
    public ConverterService(ConverterRegistry registry, Parser parser) {
        this.registry = registry;
        clientInputParser = parser;
    }

    @Override
    public String processBotResponse(String messageText) {
        try {
            return dispatch(messageText);
        } catch (ConversionException exception) {
            return exception.getMessage();
        } catch (Exception exception) {
            logger.error("Непредвиденная ошибка при обработке сообщения: ", exception);
            return UNEXPECTED_ERROR_TEXT;
        }
    }

    private String dispatch(String messageText) {
        if (messageText == null) {
            return "Я понимаю только текстовые сообщения. Напишите запрос в формате: {число} {единица1} to {единица2}";
        }

        if (messageText.equalsIgnoreCase("/start")) {
            return START_TEXT;
        }

        if (messageText.equalsIgnoreCase("/help")) {
            return HELP_TEXT;
        }

        return registry.findConverterByCommand(messageText)
            .map(QuantityConverter::getUnitsDescription)
            .orElseGet(() -> convert(messageText));
    }

    /**
     * Выполняет перевод по разобранному запросу и форматирует результат.
     *
     * @param messageText входное сообщение формата "{число} {единица1} to {единица2}"
     * @return строка результата в формате "{значение} {из} = {результат} {в}"
     * @throws ConversionException если запрос не распознан, единицы неизвестны
     *                             или единицы принадлежат разным группам
     */
    private String convert(String messageText) {
        var request = clientInputParser.parse(messageText);
        var converter = registry.findConverterByUnitName(request.from()).orElseThrow( () ->
                new ConversionException("Единицы измерения неизвестны")
        );

        return String.format(Locale.US, RESULT_TEMPLATE,
                request.value(), request.from(),
                converter.convert(request), request.to());
    }
}
