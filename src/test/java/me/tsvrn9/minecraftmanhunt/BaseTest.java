package me.tsvrn9.minecraftmanhunt;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.tsvrn9.minecraftmanhunt.features.Feature;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class BaseTest {
    private final Random random = new Random(0);
    protected WorldMock world;
    protected ServerMock server;
    protected MinecraftManhunt manhunt;
    protected PlayerMock runner, hunter;

    @BeforeEach
    public void beforeEach() {
        server = MockBukkit.mock();
        manhunt = MockBukkit.load(MinecraftManhunt.class);
        world = server.addSimpleWorld("world");

        runner = server.addPlayer();
        hunter = server.addPlayer();

        MinecraftManhunt.setRunner(runner);
    }

    @AfterEach
    public void afterEach() {
        MockBukkit.unmock();
    }

    protected WorldMock createNether() {
        WorldMock nether = server.addSimpleWorld("world_nether");
        nether.setEnvironment(World.Environment.NETHER);
        return nether;
    }

    protected Location someLocation() {
        return new Location(world, random.nextDouble(-1000, 1000), random.nextDouble(255), random.nextDouble(-1000, 1000));
    }

    protected void mockLodestoneFunctionality(ItemStack compass) {
        CompassMeta meta = mock(CompassMeta.class);
        Location[] lodestoneLocation = new Location[1];
        doAnswer(invocation -> {
            lodestoneLocation[0] = invocation.getArgument(0);
            return null;
        }).when(meta).setLodestone(any(Location.class));
        when(meta.getLodestone()).thenAnswer(_ -> lodestoneLocation[0]);
        compass.setItemMeta(meta);
    }

    protected Location someLocationIn(World world) {
        return new Location(world, random.nextDouble(-1000, 1000), random.nextDouble(255), random.nextDouble(-1000, 1000));
    }

    protected void loadFeatures(Feature... features) {
        manhunt.setFeatureRegistry(List.of(features));
        manhunt.getFeatureRegistry().registerConfigurationSerializables();
        manhunt.getFeatureRegistry().setConfig(manhunt.getConfig());
        manhunt.getFeatureRegistry().enableAll();
    }
}
