package ru.urfu.converterbot.service.models.fakeClasses;

import ru.urfu.converterbot.service.models.CustomConversion;
import ru.urfu.converterbot.service.repository.CustomConversionRepositoryImp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Заглушка {@link CustomConversionRepositoryImp} для тестов.
 * Все методы бесполезны (no-op), не хранят ничего.
 */
public class FakeCustomConversionRepository extends CustomConversionRepositoryImp {
    private final List<CustomConversion> conversions = new ArrayList<>();

    public FakeCustomConversionRepository() {
        super(null);
    }

    @Override
    public boolean saveOrUpdate(CustomConversion conversion) {
        conversions.removeIf(existing ->
                existing.chatId() == conversion.chatId()
                        && existing.fromUnit().equals(conversion.fromUnit())
                        && existing.toUnit().equals(conversion.toUnit())
                        
        );

        conversions.add(conversion);
        return true;
    }

    @Override
    public Optional<CustomConversion> findByChatIdFromAndAndTo(
            long chatId, String from, String to) {

        return conversions.stream()
                .filter(conversion -> conversion.chatId() == chatId)
                .filter(conversion -> conversion.fromUnit().equals(from))
                .filter(conversion -> conversion.toUnit().equals(to))
                .findFirst();
    }

    @Override
    public List<CustomConversion> findAllOutgoing(long chatId) {
        return conversions.stream()
                .filter(conversion -> conversion.chatId() == chatId)
                .toList();
    }

    @Override
    public boolean deleteByChatIdAndFromAndTo(
            long chatId, String from, String to) {

        return conversions.removeIf(conversion ->
                conversion.chatId() == chatId
                        && conversion.fromUnit().equals(from)
                        && conversion.toUnit().equals(to)
        );
    }

    @Override
    public List<String> findAllUnitNames(long chatId) {
        List<String> unitNames = new ArrayList<>();

        for (CustomConversion conversion : conversions) {
            if (conversion.chatId() == chatId) {
                if (!unitNames.contains(conversion.fromUnit())) {
                    unitNames.add(conversion.fromUnit());
                }

                if (!unitNames.contains(conversion.toUnit())) {
                    unitNames.add(conversion.toUnit());
                }
            }
        }

        return unitNames;
    }
}
