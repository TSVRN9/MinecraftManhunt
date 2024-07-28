package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.BaseTest;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimerTest extends BaseTest {
    @BeforeEach
    void setup() {
        loadFeatures(new Timer());
    }

    @Test
    void test3SecondTimer() {
        server.dispatchCommand(Bukkit.getConsoleSender(), "timer 3");
        long start = System.currentTimeMillis();
        server.getScheduler().waitAsyncTasksFinished();
        long end = System.currentTimeMillis();
        assertEquals(start, end, 200);
    }
}
