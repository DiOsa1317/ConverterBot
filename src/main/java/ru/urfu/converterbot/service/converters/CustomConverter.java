package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.exceptions.InvalidUserInputException;
import ru.urfu.converterbot.service.models.CustomQuantityType;
import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.repository.CustomConversionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер пользовательских (кастомных) единиц измерения.
 */
public class CustomConverter implements QuantityConverter {

    private final CustomConversionRepository conversionRepository;

    /**
     * Создаёт конвертер кастомных единиц.
     *
     * @param conversionRepository репозиторий кастомных конверсий
     */
    public CustomConverter(CustomConversionRepository conversionRepository) {
        this.conversionRepository = conversionRepository;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }

        if (!(from instanceof CustomQuantityType && to instanceof CustomQuantityType)) {
            throw new InvalidUserInputException(from + " to " + to,
                    "Кастомный конвертер поддерживает только кастомные единицы.");
        }

        var customFrom = (CustomQuantityType) from;
        var customTo = (CustomQuantityType) to;
        long chatId = customFrom.chatId();
        var conv = conversionRepository.findByChatIdFromAndAndTo(
                chatId, customFrom.name(), customTo.name());
        if (conv.isPresent()) {
            return value.multiply(conv.get().rate())
                    .setScale(10, RoundingMode.HALF_UP);
        }
        var reverse = conversionRepository.findByChatIdFromAndAndTo(
                chatId, customTo.name(), customFrom.name());
        if (reverse.isPresent()) {
            return value.divide(reverse.get().rate(), 10, RoundingMode.HALF_UP);
        }
        throw new InvalidUserInputException(
                customFrom.name() + " to " + customTo.name(),
                "Нет прямого курса между " + customFrom.name() + " и " + customTo.name());
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        return new LinkedHashMap<>();
    }

    @Override
    public QuantityType baseUnit() {
        return null;
    }
}
