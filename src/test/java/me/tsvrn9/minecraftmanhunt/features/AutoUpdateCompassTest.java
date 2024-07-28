package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.WorldMock;
import me.tsvrn9.minecraftmanhunt.BaseTest;
import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoUpdateCompassTest extends BaseTest {
    final long enoughTicks = 30L;

    @BeforeEach
    void setup() {
        manhunt.setFeatureRegistry(List.of(
                new AutoUpdateCompass()
        ));
        manhunt.getFeatureRegistry().registerConfigurationSerializables();
        manhunt.getFeatureRegistry().setConfig(manhunt.getConfig());
        manhunt.getFeatureRegistry().enableAll();
    }

    @Test
    void testTimer() {
        runner.setLocation(someLocation());
        hunter.setLocation(someLocation());

        server.getScheduler().performTicks(enoughTicks); // task should have run by now

        assertEquals(runner.getLocation(), hunter.getCompassTarget());
    }

    @Test
    void testUpdateInLastKnownLocation() {
        WorldMock nether = createNether();

        runner.setLocation(someLocation());
        hunter.setLocation(someLocation());

        Location lastKnownLocation = runner.getLocation();
        runner.teleport(someLocationIn(nether));

        server.getPluginManager().assertEventFired(PlayerTeleportEvent.class);

        server.getScheduler().performTicks(enoughTicks);

        assertEquals(lastKnownLocation, hunter.getCompassTarget());
    }

    @Test
    void testUpdateInNether() {
        WorldMock nether = createNether();

        runner.setLocation(someLocationIn(nether));
        hunter.setLocation(someLocationIn(nether));

        MinecraftManhunt.giveHunterGear(hunter);

        ItemStack compass = hunter.getInventory().getItem(0);
        assert compass != null;
        mockLodestoneFunctionality(compass);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();

        server.getScheduler().performTicks(enoughTicks);

        assertEquals(runner.getLocation(), meta.getLodestone());
    }
}
