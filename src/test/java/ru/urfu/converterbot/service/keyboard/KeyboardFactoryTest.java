package ru.urfu.converterbot.service.keyboard;

import org.junit.jupiter.api.Test;
import ru.urfu.converterbot.keyboard.KeyboardFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyboardFactoryTest {
    private final KeyboardFactory factory = new KeyboardFactory();

    @Test
    void mainMenuHasFourButtons() {
        var view = factory.createMainMenu();
        assertEquals(1, view.rows().size());
        assertEquals(4, view.rows().get(0).buttons().size());
    }
}
