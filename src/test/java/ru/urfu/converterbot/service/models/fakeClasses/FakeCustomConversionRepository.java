package ru.urfu.converterbot.service.models.fakeClasses;

import ru.urfu.converterbot.service.CustomConversionRepository;
import ru.urfu.converterbot.service.models.CustomConversion;

import java.sql.SQLException;
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
    public void saveOrUpdate(CustomConversion conversion) throws SQLException {}

    @Override
    public Optional<CustomConversion> findByChatIdFromAndAndTo(long chatId, String from, String to)
            throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<CustomConversion> findAllOutgoing(long chatId) throws SQLException {
        return List.of();
    }

    @Override
    public boolean deleteByChatIdAndFromAndTo(long chatId, String from, String to) throws SQLException {
        return false;
    }

    @Override
    public List<String> findAllUnitNames(long chatId) throws SQLException {
        return List.of();
    }
}
