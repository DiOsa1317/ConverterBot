package ru.urfu.converterbot.service;

import ru.urfu.converterbot.service.models.LengthType;
import ru.urfu.converterbot.service.models.ConversionRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Конвертер длины. Все переводы идут через метры как базовую единицу,
 * поэтому перевод в обе стороны симметричен и не требует таблицы пар.
 */
public class LengthConverter implements QuantityConverter {

    /** Сколько метров содержится в одной единице измерения. */
    private final Map<LengthType, BigDecimal> metersInUnit = new EnumMap<>(LengthType.class);

   /**
    * Создаёт конвертер и заполняет таблицу переводов единиц в метры.
    */
   public LengthConverter()  {
        metersInUnit.put(LengthType.METER, new BigDecimal("1.0"));
        metersInUnit.put(LengthType.KILOMETER, new BigDecimal("1000.0"));
        metersInUnit.put(LengthType.MILE, new BigDecimal("1609.344"));
        metersInUnit.put(LengthType.CENTIMETER, new BigDecimal("0.01"));
        metersInUnit.put(LengthType.INCH, new BigDecimal("0.0254"));
    }

    /**
     * Возвращает название группы для меню и текста справки.
     *
     * @return название группы — «Длина»
     */
    @Override
    public String title() {
        return "Длина";
    }

    /**
     * Возвращает команду, по которой бот показывает справку по группе.
     *
     * @return команда {@code /length}
     */
    @Override
    public String command() {
        return "/length";
    }

    /**
     * Возвращает enum с единицами группы — по нему реестр определяет владельца запроса.
     *
     * @return класс enum с типами длины
     */
    @Override
    public Class<LengthType> quantityType() {
        return LengthType.class;
    }

    @Override
    public BigDecimal convert(ConversionRequest request) {
        if (!(request.from() instanceof LengthType from)
            || !(request.to() instanceof LengthType to)) {
            throw new IllegalArgumentException(
                    "LengthConverter не может обработать запрос: " + request);
        }
        return request.value().multiply(metersInUnit.get(from))
                .divide(metersInUnit.get(to), 10, RoundingMode.HALF_UP);
    }

    @Override
    public String getUnitsDescription() {
        return String.format(Locale.US, """
                Величины длин относительно метров:
                Километр = %f м;
                Миля = %f м;
                Дюйм = %f м;
                """,
                metersInUnit.get(LengthType.KILOMETER),
                metersInUnit.get(LengthType.MILE),
                metersInUnit.get(LengthType.INCH));
    }
}
