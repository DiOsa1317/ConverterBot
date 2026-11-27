package ru.urfu.converterbot.service.repository;

import ru.urfu.converterbot.service.models.CustomConversion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomConversionRepository {
    /**
     * Сохраняет новый кастомный перевод
     *
     * @param conversion данные о кастомном переводе
     * @return true, если операция успешно выполнена
     */
    boolean save(CustomConversion conversion);

    /**
     * Ищет кастомный перевод пользователя.
     *
     * @param chatId ID чата
     * @param from исходная единица
     * @param to целевая единица
     * @return найденный перевод или Optional.empty() при отсутствии или ошибке
     */
    Optional<CustomConversion> findByChatIdFromAndAndTo(
            long chatId, String from, String to
    );

    /**
     * Возвращает все кастомные переводы пользователя.
     *
     * @param chatId ID чата
     * @return список кастомных переводов или пустой список при ошибке
     */
    List<CustomConversion> findAllOutgoing(long chatId);

    /**
     * Удаляет кастомный перевод пользователя.
     *
     * @param chatId ID чата
     * @param from исходная единица
     * @param to целевая единица
     * @return true, если перевод удалён
     */
    boolean deleteByChatIdAndFromAndTo(
            long chatId, String from, String to
    );

    /**
     * Обновляет кастомный перевод пользователя.
     *
     * @param chatId ID чата
     * @param from исходная единица
     * @param to целевая единица
     * @param rate новый курс
     * @return true, если перевод удалён
     */
    boolean updateCustomConversion(
            long chatId, String from, String to, BigDecimal rate
    );

    /**
     * Возвращает все названия кастомных единиц пользователя.
     *
     * @param chatId ID чата
     * @return список названий или пустой список при ошибке
     */
    List<String> findAllUnitNames(long chatId);
}
