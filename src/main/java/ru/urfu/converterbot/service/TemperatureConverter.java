package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.ConversionRequest;
import ru.urfu.converterbot.service.models.TemperatureType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Конвертер температуры. Переводит через градусы Цельсия как промежуточную шкалу,
 * потому что перевод между °C, °F и K является аффинным преобразованием, а не линейным:
 * результат нельзя получить умножением исходного значения на коэффициент.
 */
public class TemperatureConverter implements QuantityConverter {

    /** Сколько градусов Цельсия в одном градусе Фаренгейта. */
    private static final BigDecimal FAHRENHEIT_IN_CELSIUS = new BigDecimal("1.8");

    /** Смещение шкалы Фаренгейта относительно шкалы Цельсия. */
    private static final BigDecimal FAHRENHEIT_OFFSET = new BigDecimal("32.0");

    /** Абсолютный ноль в градусах Цельсия — база шкалы Кельвина. */
    private static final BigDecimal ABSOLUTE_ZERO_IN_CELSIUS = new BigDecimal("273.15");

    /**
     * Возвращает название группы для меню и текста справки.
     *
     * @return название группы — «Температура»
     */
    @Override
    public String title() {
        return "Температура";
    }

    /**
     * Возвращает команду, по которой бот показывает справку по группе.
     *
     * @return команда {@code /temperature}
     */
    @Override
    public String command() {
        return "/temperature";
    }

    /**
     * Возвращает enum с единицами группы — по нему реестр определяет владельца запроса.
     *
     * @return класс enum со шкалами температуры
     */
    @Override
    public Class<TemperatureType> quantityType() {
        return TemperatureType.class;
    }

    /**
     * Возвращает короткую подпись шкалы для кнопки меню.
     * Кельвину соответствует имя константы, принятое в запросе пользователя.
     *
     * @param unit имя шкалы, например {@code CELSIUS}
     * @return подпись «°C», «°F» или имя константы
     */
    @Override
    public String displayName(String unit) {
        return switch (unit) {
            case "CELSIUS" -> "°C";
            case "FAHRENHEIT" -> "°F";
            default -> unit;
        };
    }

    /**
     * Переводит температуру через градусы Цельсия: значение сначала
     * приводится к Цельсию, затем выражается в целевой шкале.
     *
     * @param request запрос с исходным значением и обеими шкалами группы
     * @return переведённое значение; при одинаковых шкалах — исходное значение
     */
    @Override
    public BigDecimal convert(ConversionRequest request) {
        var from = (TemperatureType) request.from();
        var to = (TemperatureType) request.to();
        if (from == to) {
            return request.value();
        }
        return fromCelsius(toCelsius(request.value(), from), to);
    }

    /**
     * Приводит исходное значение к градусам Цельсия.
     *
     * @param value исходное значение температуры
     * @param unit  шкала, в которой задано исходное значение
     * @return то же значение в градусах Цельсия
     */
    private BigDecimal toCelsius(BigDecimal value, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS ->  value;
            case FAHRENHEIT -> value.subtract(FAHRENHEIT_OFFSET).divide(FAHRENHEIT_IN_CELSIUS, 10, RoundingMode.HALF_UP);
            case KELVIN -> value.subtract(ABSOLUTE_ZERO_IN_CELSIUS);
        };
    }

    /**
     * Выражает значение в градусах Цельсия в целевой шкале.
     *
     * @param celsius значение в градусах Цельсия
     * @param unit    целевая шкала
     * @return значение в целевой шкале
     */
    /**
     * Выражает значение в градусах Цельсия в целевой шкале.
     *
     * @param celsius значение в градусах Цельсия
     * @param unit    целевая шкала
     * @return значение в целевой шкале
     */
    private BigDecimal fromCelsius(BigDecimal celsius, TemperatureType unit) {
        return switch (unit) {
            case CELSIUS -> celsius;
            case FAHRENHEIT -> celsius.multiply(FAHRENHEIT_IN_CELSIUS).add(FAHRENHEIT_OFFSET);
            case KELVIN -> celsius.add(ABSOLUTE_ZERO_IN_CELSIUS);
        };
    }

    /**
     * Возвращает текст с формулами связи шкал температуры.
     *
     * @return справка о переводе через градусы Цельсия
     */
    @Override
    public String getActualCourse() {
        return """
                Cвязь температур:
                °C → °F (значение * 1.8 + 32);
                °C → K (значение + 273.15);
                """;
    }
}
