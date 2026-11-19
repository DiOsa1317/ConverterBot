package ru.urfu.converterbot.service.repository;

import ru.urfu.converterbot.service.models.ConversionHistory;

import java.util.List;


public interface ConversionHistoryRepository {
    /**
     * Сохраняет новую запись в истории конвертаций.
     *
     * @param chatId ID чата с пользователем
     * @param input исходный запрос пользователя
     * @param result результат конвертации
     * @return true, если запись успешно сохранена
     */
    boolean save(long chatId, String input, String result);

    /**
     * Возвращает страницу истории пользователя.
     *
     * @param chatId ID чата с пользователем
     * @param limit количество записей на странице
     * @param offset количество записей, которые нужно пропустить
     * @return список записей истории или пустой список при ошибке
     */
    List<ConversionHistory> findPage(long chatId, int limit, int offset);

    /**
     * Возвращает количество записей истории пользователя.
     *
     * @param chatId ID чата с пользователем
     * @return количество записей или 0 при ошибке
     */
    int count(long chatId);

    /**
     * Удаляет всю историю конвертаций пользователя.
     *
     * @param chatId ID чата с пользователем
     * @return true, если была удалена хотя бы одна запись
     */
    boolean deleteAll(long chatId);
}