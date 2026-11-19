package ru.urfu.converterbot.service.models.fakeClasses;

import ru.urfu.converterbot.service.models.ConversionHistory;
import ru.urfu.converterbot.service.repository.ConversionHistoryRepository;

import java.util.List;

/**
 * Заглушка {@link ConversionHistoryRepository} для тестов.
 * Методы не взаимодействуют с БД и не хранят данные.
 */
public class FakeConversionHistoryRepository extends ConversionHistoryRepository {

    public FakeConversionHistoryRepository() {
        super(null);
    }

    @Override
    public boolean save(long chatId, String input, String result) {
        return true;
    }

    @Override
    public List<ConversionHistory> findPage(long chatId, int limit, int offset) {
        return List.of();
    }

    @Override
    public int count(long chatId) {
        return 0;
    }

    @Override
    public boolean deleteAll(long chatId) {
        return false;
    }
}