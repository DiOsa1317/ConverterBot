package com.example.echobot.service;

import java.util.Locale;

import com.example.echobot.service.models.CurrencyType;
import com.example.echobot.service.models.PhysicalQuantities;
import com.example.echobot.service.models.QuantityModel;

public class ConverterService implements BotResponseProcessor {

    private final PhysicalQuantitiesConverter physicalQuantitiesConverter;
    private final CurrencyConverter currencyConverter;
    private final ClientInputParser clientInputParser;

    public ConverterService() {
        physicalQuantitiesConverter = new PhysicalQuantitiesConverter();
        currencyConverter = new CurrencyConverter();
        clientInputParser = new ClientInputParser();
    }

    /**
     * Обрабатывает сообщение пользователя и выполняет конвертацию валют.
     *
     * @param messageText входное сообщение формата "{сумма} {валюта1} to {валюта2}",
     *                    например "100 USD to EUR", или команды /currency, /physical
     * @return строка с результатом конвертации в формате "{сумма} {валюта1} = {результат} {валюта2}" 
     *                      или инфа о доступных переводах
     */
    @Override
    public String processBotResponse(String messageText) {
        if (messageText.equalsIgnoreCase("/currency")) {
            return currencyConverter.getActualCourse();
        }

        if (messageText.equalsIgnoreCase("/physical")) {
            return physicalQuantitiesConverter.getActualCourse();
        }

        QuantityModel quantityModel = clientInputParser.parse(messageText);

        double convertedValue;
        if (quantityModel.from() instanceof CurrencyType) {
            convertedValue = currencyConverter.convertFirstCurrencyToSecond(quantityModel);
        } else if (quantityModel.from() instanceof PhysicalQuantities) {
            convertedValue = physicalQuantitiesConverter.convertPhysicalQuantities(quantityModel);
        } else {
            throw new IllegalArgumentException("Неизвестный тип единицы измерения");
        }

        return String.format(Locale.US,"%.4f %s = %.4f %s",
                quantityModel.value(), quantityModel.from(), convertedValue, quantityModel.to());
    }  
}
