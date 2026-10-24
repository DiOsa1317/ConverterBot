package ru.urfu.converterbot.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.urfu.converterbot.service.converters.CurrencyConverter;
import ru.urfu.converterbot.service.converters.LengthConverter;
import ru.urfu.converterbot.service.converters.QuantityConverter;
import ru.urfu.converterbot.service.converters.TemperatureConverter;
import ru.urfu.converterbot.service.converters.WeightConverter;
import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.*;

/**
 * Диспетчер перевода величин. Принимает текстовое сообщение пользователя,
 * определяет группу величин через реестр и возвращает готовый ответ для чата.
 * Реализует интерфейс {@link BotResponseProcessor}.
 */
public class BotService implements BotResponseProcessor {

    private final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final CurrencyConverter currencyConverter;

    private final LengthConverter lengthConverter;

    private final WeightConverter weightConverter;

    private final TemperatureConverter temperatureConverter;

    private final Parser clientInputParser;

    private final MessageFormatter messageFormatter;

    /**
     * Создаёт диспетчер с явно переданными конвертерами всех групп.
     *
     * @param currencyConverter конвертер валют
     * @param lengthConverter конвертер длины
     * @param weightConverter конвертер массы
     * @param temperatureConverter конвертер температуры
     * @param parser парсер ввода пользователя
     */
    public BotService(@NotNull CurrencyConverter currencyConverter,
                            @NotNull LengthConverter lengthConverter,
                            @NotNull WeightConverter weightConverter,
                            @NotNull TemperatureConverter temperatureConverter,
                            @NotNull Parser parser,
                            @NotNull MessageFormatter messageFormatter) {
        this.currencyConverter = currencyConverter;
        this.lengthConverter = lengthConverter;
        this.weightConverter = weightConverter;
        this.temperatureConverter = temperatureConverter;
        this.clientInputParser = parser;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public String processBotResponse(String messageText) {
        try {
            return dispatch(messageText);
        } catch (IllegalArgumentException exception) {
            logger.error("Ошибка при обработке аргументов: ", exception);
            return messageFormatter.invalidInput();
        } catch (Exception exception) {
            logger.error("Непредвиденная ошибка при обработке сообщения: ", exception);
            return messageFormatter.unexpectedError();
        }
    }

    private String dispatch(@Nullable String messageText) throws InvalidUserInputException {
        if (messageText == null) {
            return messageFormatter.noText();
        }
        if (messageText.equalsIgnoreCase("/start")) {
            return messageFormatter.start();
        }
        if (messageText.equalsIgnoreCase("/help")) {
            return messageFormatter.help();
        }
        for (BotCommand command : BotCommand.values()) {
        if (command.matches(messageText)) {
            if (command == BotCommand.CURRENCY) {
                return messageFormatter.currencyRates(
                    currencyConverter.baseUnit(),
                    currencyConverter.getReferenceRates(),
                    currencyConverter.lastUpdated());
            }
            
            var converter = converterFor(command);
            return messageFormatter.rates(command.title(),
                    converter.baseUnit(), converter.getReferenceRates());
        }
    }
        return convert(messageText);
    }

    /**
     * Возвращает конвертер, соответствующий команде справки.
     *
     * @param command команда группы величин
     * @return конвертер этой группы
     */
    private QuantityConverter converterFor(BotCommand command) {
        return switch (command) {
            case CURRENCY -> currencyConverter;
            case LENGTH -> lengthConverter;
            case WEIGHT -> weightConverter;
            case TEMPERATURE -> temperatureConverter;
        };
    }

    /**
     * Выполняет перевод по разобранному запросу и форматирует результат.
     *
     * @param messageText входное сообщение формата "{число} {единица1} to {единица2}"
     * @return строка результата в формате "{значение} {из} = {результат} {в}"
     * @throws InvalidUserInputException если запрос не распознан, единицы неизвестны
     *                                   или принадлежат разным группам
     */
    private String convert(String messageText) throws InvalidUserInputException {
        var request = clientInputParser.parse(messageText);
        var result = converterFor(request.from().group())
            .convert(request.value(), request.from(), request.to());

        return messageFormatter.getResult(request, result);
    }
}
