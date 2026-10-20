package ru.urfu.converterbot.service.converters;

import ru.urfu.converterbot.service.converters.models.TemperatureData;
import ru.urfu.converterbot.service.models.QuantityType;
import ru.urfu.converterbot.service.models.TemperatureType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конвертер температуры.
 */
public class TemperatureConverter implements QuantityConverter {

    private final TemperatureData temperatureData;

    /**
     * Создаёт конвертер, получая данные о соотношении температур
     */
    public TemperatureConverter(TemperatureData temperatureData) {
        this.temperatureData = temperatureData;
    }

    @Override
    public BigDecimal convert(BigDecimal value, QuantityType from, QuantityType to) {
        if (from == to) {
            return value;
        }

        BigDecimal celsius = value
                .add(temperatureData.toCelsiusOffset(from))
                .multiply(temperatureData.toCelsiusRate(from));

        return celsius
                .multiply(temperatureData.fromCelsiusRate(to))
                .add(temperatureData.fromCelsiusOffset(to))
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    @Override
    public Map<QuantityType, BigDecimal> getReferenceRates() {
        var rates = new LinkedHashMap<QuantityType, BigDecimal>();

        for (TemperatureType unit : TemperatureType.values()) {
            rates.put(
                    unit,
                    convert(BigDecimal.ONE, baseUnit(), unit)
            );
        }

        return rates;
    }

    @Override
    public QuantityType baseUnit() {
        return TemperatureType.CELSIUS;
    }
}
