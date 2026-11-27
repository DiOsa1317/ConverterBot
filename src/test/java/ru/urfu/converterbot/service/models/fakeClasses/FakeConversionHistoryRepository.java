package ru.urfu.converterbot.service.models.fakeClasses;

import ru.urfu.converterbot.service.models.ConversionHistory;
import ru.urfu.converterbot.service.repository.ConversionHistoryRepositoryImp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FakeConversionHistoryRepository extends ConversionHistoryRepositoryImp {

    private final List<ConversionHistory> history = new ArrayList<>();

    public FakeConversionHistoryRepository() {
        super(null);
    }

    @Override
    public boolean save(long chatId, String input, String result) {
        history.add(new ConversionHistory(
                1,
                chatId,
                input,
                result,
                LocalDateTime.of(2026, 11, 25, 12, 13)
        ));
        return true;
    }

    @Override
    public List<ConversionHistory> findPage(long chatId, int limit, int offset) {
        return history.stream()
                .filter(item -> item.chatId() == chatId)
                .sorted(Comparator.comparing(ConversionHistory::createdAt)
                        .reversed()
                        .thenComparing(ConversionHistory::id, Comparator.reverseOrder()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public int count(long chatId) {
        return (int) history.stream()
                .filter(item -> item.chatId() == chatId)
                .count();
    }

    @Override
    public boolean deleteAll(long chatId) {
        return history.removeIf(item -> item.chatId() == chatId);
    }
}
