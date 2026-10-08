package ru.urfu.converterbot.service.models;

/**
 * Перечисление поддерживаемых типов валют
 */
public enum CurrencyType implements QuantityType {
    /** Российский рубль — базовая валюта, курс к самому себе равен 1. */
    RUB,
    /** Европейский евро. */
    EUR,
    /** Американский доллар. */
    USD,
    /** Китайский юань. */
    CNY,
    /** Казахстанский тенге. */
    KZT
}
