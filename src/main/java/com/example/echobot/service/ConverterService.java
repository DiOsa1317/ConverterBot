package com.example.echobot.service;

import java.util.Locale;

import com.example.echobot.service.models.QuantityModel;

public class ConverterService implements BotResponseProcessor {

    /** Шаблон строки с результатом перевода. */
    private static final String RESULT_TEMPLATE = "%.4f %s = %.4f %s";

    private final ConverterRegistry registry;
    private final ClientInputParser clientInputParser;

    public ConverterService(ConverterRegistry registry) {
        this.registry = registry;
        clientInputParser = new ClientInputParser();
    }

    /**
     * Обрабатывает сообщение пользователя
     *
     * @param messageText входное сообщение формата "{сумма} {единица1} to {единица2}",
     *                    например "100 USD to EUR", или команды для выбора типа перевода (например, /currency)
     * @return строка с результатом конвертации в формате "{сумма} {единица1} = {результат} {единица2}"
     *                      или инфа о доступных переводах
     *  @throws ConversionException если запрос не распознан или единицы из разных групп
     */
    @Override
    public String processBotResponse(String messageText) {
        var helpConverter = registry.findConverterByCommand(messageText).orElse(null);
        if (helpConverter != null) {
            return helpConverter.getActualCourse();
        }
        return convert(messageText);
    }

    public String convert(String messageText) {
        var request = clientInputParser.parse(messageText);
        var converter = registry.findConverterByUnitName(request.from()).orElseThrow( () ->
                new ConversionException("Не найден конвертер для группы: " + request.from())
        );
        checkSameGroup(converter, request);
        return String.format(Locale.US, RESULT_TEMPLATE,
                request.value(), request.from(),
                converter.convert(request), request.to());
    }

    private void checkSameGroup(QuantityConverter converter, QuantityModel request) {
        if (!converter.quantityType().isInstance(request.to())) {
            throw new ConversionException(
                    ("Нельзя перевести %s в %s: это разные группы величин.%n"
                    + "В группе %s доступны единицы: %s")
                            .formatted(request.from(), request.to(),
                                    converter.title(), String.join(", ", converter.units()))
            );
        }
    }

    /**
     * Возвращает справку по группе величин, запрошенной командой.
     *
     * @param command команда справки, например {@code /currency}
     * @return текст со списком коэффициентов группы
     */
    public String getHelp(String command) {
        return registry.findConverterByCommand(command)
                .orElseThrow(() -> new ConversionException("Неизвестная команда: " + command))
                .getActualCourse();
    }
}
