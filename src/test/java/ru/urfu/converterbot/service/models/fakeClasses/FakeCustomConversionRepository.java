package ru.urfu.converterbot.service.models.fakeClasses;

import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.repository.CustomConversionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Заглушка {@link CustomConversionRepository} для тестов.
 * Все методы бесполезны (no-op), не хранят ничего.
 */
public class FakeCustomConversionRepository extends CustomConversionRepository {

    public FakeCustomConversionRepository() {
        super(null);
    }

    @Override
    public boolean saveOrUpdate(CustomConversion conversion) {
        return true;
    }

    @Override
    public Optional<CustomConversion> findByChatIdFromAndAndTo(long chatId, String from, String to) {
        return Optional.empty();
    }

    @Override
    public List<CustomConversion> findAllOutgoing(long chatId) {
        return List.of();
    }

    @Override
    public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) {
        return false;
    }

    @Override
    public List<String> findAllUnitNames(long chatId) {
        return List.of();
    }
}
