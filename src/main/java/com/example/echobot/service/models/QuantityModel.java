package com.example.echobot.service.models;

/**
 * Результат разбора запроса на конвертацию: число и единицы измерения одного типа,
 * между которыми нужно выполнить перевод.
 *
 * @param value числовое значение, которое нужно перевести
 * @param from единица измерения, из которой переводим
 * @param to единица измерения, в которую переводим
 */
public record QuantityModel(double value, QuantityType from, QuantityType to) {
}

