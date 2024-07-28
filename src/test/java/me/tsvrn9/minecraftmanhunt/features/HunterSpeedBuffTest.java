package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.BaseTest;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HunterSpeedBuffTest extends BaseTest {
    final long enoughTicks = 70L;

    @BeforeEach
    void setup() {
        loadFeatures(new HunterSpeedBuff());
    }

    @Test
    void noSpeedShouldBeGiven() {
        runner.setLocation(new Location(world, 0, 0, 0));
        hunter.setLocation(new Location(world, 3, 0, 3));

        server.getScheduler().performTicks(enoughTicks);

        assertTrue(hunter.getActivePotionEffects().isEmpty());
    }

    @Disabled
    @Test
    void speedShouldBeGiven() {
        runner.setLocation(new Location(world, 0, 0, 0));
        hunter.setLocation(new Location(world, 10000, 0, 10000));

        server.getScheduler().performTicks(enoughTicks);

        assertFalse(hunter.getActivePotionEffects().isEmpty());
    }

    @Disabled
    @Test
    void shouldDowngradeSpeed() {
        runner.setLocation(new Location(world, 0, 0, 0));
        hunter.setLocation(new Location(world, 10000, 0, 10000));

        server.getScheduler().performTicks(enoughTicks);

        assertFalse(hunter.getActivePotionEffects().isEmpty());

        runner.setLocation(new Location(world, 0, 0, 0));
        hunter.setLocation(new Location(world, 3, 0, 3));

        server.getScheduler().performTicks(enoughTicks);

        assertTrue(hunter.getActivePotionEffects().isEmpty());
    }
}
