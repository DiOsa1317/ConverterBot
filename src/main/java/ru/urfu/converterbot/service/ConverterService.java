package ru.urfu.converterbot.service;

import java.util.Locale;

import ru.urfu.converterbot.service.models.ConversionRequest;

/**
 * Диспетчер перевода величин. Принимает текстовое сообщение пользователя,
 * определяет группу величин через реестр и возвращает готовый ответ для чата.
 * Реализует интерфейс {@link BotResponseProcessor}.
 */
public class ConverterService implements BotResponseProcessor {

    /** Шаблон строки с результатом перевода. */
    private static final String RESULT_TEMPLATE = "%.4f %s = %.4f %s";

    /** Реестр конвертеров, по которому определяется группа величин. */
    private final ConverterRegistry registry;

    /** Парсер ввода пользователя: превращает текст в запрос на перевод. */
    private final ClientInputParser clientInputParser;

    /**
     * Создаёт диспетчер с собственным парсером ввода.
     *
     * @param registry реестр конвертеров, в котором ищется группа величин
     */
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
     * @throws ConversionException если запрос не распознан или единицы из разных групп
     */
    @Override
    public String processBotResponse(String messageText) {
        var helpConverter = registry.findConverterByCommand(messageText).orElse(null);
        if (helpConverter != null) {
            return helpConverter.getActualCourse();
        }
        return convert(messageText);
    }

    /**
     * Выполняет перевод по разобранному запросу и форматирует результат.
     *
     * @param messageText входное сообщение формата "{число} {единица1} to {единица2}"
     * @return строка результата в формате "{значение} {из} = {результат} {в}"
     * @throws ConversionException если запрос не распознан, единицы неизвестны
     *                             или единицы принадлежат разным группам
     */
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

    /**
     * Проверяет, что обе единицы запроса принадлежат одной группе величин.
     *
     * @param converter конвертер, найденный по исходной единице
     * @param request   разобранный запрос пользователя
     * @throws ConversionException если целевая единица принадлежит другой группе
     */
    private void checkSameGroup(QuantityConverter converter, ConversionRequest request) {
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
     * @throws ConversionException если команда не соответствует ни одной группе
     */
    public String getHelp(String command) {
        return registry.findConverterByCommand(command)
                .orElseThrow(() -> new ConversionException("Неизвестная команда: " + command))
                .getActualCourse();
    }
}
