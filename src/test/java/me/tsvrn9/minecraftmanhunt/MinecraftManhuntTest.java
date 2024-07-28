package me.tsvrn9.minecraftmanhunt;

import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftManhuntTest extends BaseTest {
    @Test
    void testSettingRunner() {
        assertEquals(runner, MinecraftManhunt.getRunner());
        assertNotEquals(hunter, MinecraftManhunt.getRunner());
        assertNotEquals(hunterTwo, MinecraftManhunt.getRunner());
    }

    @Test
    void testCompass() {
        runner.setLocation(someLocation());
        hunter.setLocation(someLocation());
        hunterTwo.setLocation(someLocation());

        MinecraftManhunt.updateHunterCompass(hunter);
        MinecraftManhunt.updateHunterCompass(hunterTwo);

        assertEquals(runner.getLocation(), hunter.getCompassTarget());
        assertEquals(runner.getLocation(), hunterTwo.getCompassTarget());
    }

    @Test
    void testLastKnownLocation() {
        WorldMock nether = createNether();

        runner.setLocation(someLocation());
        hunter.setLocation(someLocation());

        Location preTeleportLocation = runner.getLocation();
        runner.teleport(someLocationIn(nether), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);

        MinecraftManhunt.updateHunterCompass(hunter);

        server.getPluginManager().assertEventFired(PlayerTeleportEvent.class);
        assertEquals(preTeleportLocation, hunter.getCompassTarget());
    }

    @Test
    void testCompassInNether() {
        WorldMock nether = createNether();

        runner.setLocation(someLocationIn(nether));
        hunter.setLocation(someLocationIn(nether));

        MinecraftManhunt.giveHunterGear(hunter);

        ItemStack compass = hunter.getInventory().getItem(0);
        assert compass != null;
        mockLodestoneFunctionality(compass);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();

        MinecraftManhunt.updateHunterCompass(hunter);

        assertEquals(runner.getLocation(), meta.getLodestone());
    }

}
