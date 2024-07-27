package me.tsvrn9.minecraftmanhunt;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftManhuntTest {
    Random random;
    ServerMock server;
    MinecraftManhunt manhunt;
    PlayerMock runner, hunter, hunterTwo;
    WorldMock world;

    @BeforeEach
    void setUp() {
        random = new Random(0); // constant seed for reproducibility

        server = MockBukkit.mock();
        manhunt = MockBukkit.load(MinecraftManhunt.class);
        world = server.addSimpleWorld("world");

        runner = server.addPlayer();
        hunter = server.addPlayer();
        hunterTwo = server.addPlayer();

        MinecraftManhunt.setRunner(runner);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSettingRunner() {
        assertEquals(runner, MinecraftManhunt.getRunner());
        assertNotEquals(hunter, MinecraftManhunt.getRunner());
        assertNotEquals(hunterTwo, MinecraftManhunt.getRunner());
    }

    @Test
    void testCompass() {
        runner.setLocation(someLocationIn(world));
        hunter.setLocation(someLocationIn(world));
        hunterTwo.setLocation(someLocationIn(world));

        MinecraftManhunt.updateHunterCompass(hunter);
        MinecraftManhunt.updateHunterCompass(hunterTwo);

        assertEquals(runner.getLocation(), hunter.getCompassTarget());
        assertEquals(runner.getLocation(), hunterTwo.getCompassTarget());
    }

    @Test
    void testLastKnownLocation() {
        WorldMock nether = server.addSimpleWorld("world_nether");
        nether.setEnvironment(World.Environment.NETHER);

        runner.setLocation(someLocationIn(world));
        hunter.setLocation(someLocationIn(world));

        Location preTeleportLocation = runner.getLocation();
        runner.teleport(someLocationIn(nether), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);

        MinecraftManhunt.updateHunterCompass(hunter);

        server.getPluginManager().assertEventFired(PlayerTeleportEvent.class);
        assertEquals(preTeleportLocation, hunter.getCompassTarget());
    }

    Location someLocationIn(World world) {
        return new Location(world, random.nextDouble(-1000, 1000), random.nextDouble(255), random.nextDouble(-1000, 1000));
    }
}
