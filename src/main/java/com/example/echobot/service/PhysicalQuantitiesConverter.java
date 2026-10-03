package com.example.echobot.service;

import static com.example.echobot.service.models.PhysicalQuantities.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.echobot.service.models.PhysicalQuantities;
import com.example.echobot.service.models.QuantityModel;

/**
 * Конвертер физических величин для обработки запросов на конвертацию.
 * Использует фиксированные данные о величинах.
 */

public class PhysicalQuantitiesConverter {

    private static final Map<PhysicalQuantities, Double> RATES_QUANTITIES = new HashMap<>();

    static {
        RATES_QUANTITIES.put(KM, 0.6214);
        RATES_QUANTITIES.put(MI, 1.6093);
        RATES_QUANTITIES.put(KG, 2.2046);
        RATES_QUANTITIES.put(LB, 0.4536);
        RATES_QUANTITIES.put(C, 33.8);
        RATES_QUANTITIES.put(F, 0.0296);
    }

    /**
     * Возвращает строку с актуальными данными о соотношении физических величин.
     *
     * @return информация о всех доступных соотношениях физических величин
     */
    public String getActualCourse() {
        return String.format(Locale.US, """
                Доступны переводы по таким величинам:
                Километры в мили - %.4f;
                Килограммы в фунты - %.4f;
                Градусы Цельсия в Градусы по Фаренгейту - %.4f;
                """,
                RATES_QUANTITIES.get(PhysicalQuantities.KM),
                RATES_QUANTITIES.get(PhysicalQuantities.KG),
                RATES_QUANTITIES.get(PhysicalQuantities.C));
    }

    /**
     * Конвертирует физическую величину из одной в другую.
     *
     * @param from   исходная величина
     * @param to     целевая величина
     * @param amount значение для конвертации
     * @return конвертированное значение
     */
    public Double convertPhysicalQuantities(QuantityModel quantityModel) {
        if (quantityModel.from() == quantityModel.to())
            return quantityModel.value();
        return RATES_QUANTITIES.get(quantityModel.from()) * quantityModel.value();
    }
}
