package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.WorldMock;
import me.tsvrn9.minecraftmanhunt.BaseTest;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.util.StructureSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RunnerFortressTrackingTest extends BaseTest {
    @BeforeEach
    void setup() {
        loadFeatures(new RunnerFortressTracking());
    }

    @Test
    void ensureTrackingWorks() {
        WorldMock nether = spy(createNether());
        StructureSearchResult searchResult = mock(StructureSearchResult.class);

        when(searchResult.getLocation()).thenReturn(someLocationIn(nether));
        doReturn(searchResult).when(nether)
                .locateNearestStructure(any(), eq(Structure.FORTRESS), anyInt(), anyBoolean());

        ItemStack compass = new ItemStack(Material.COMPASS);
        mockLodestoneFunctionality(compass);

        runner.setLocation(someLocationIn(nether));
        runner.getInventory().addItem(compass);

        // simulate right click with compass
        PlayerInteractEvent event = new PlayerInteractEvent(
                runner,
                Action.RIGHT_CLICK_AIR,
                compass,
                null,
                BlockFace.UP
        );

        server.getPluginManager().callEvent(event);
        server.getPluginManager().assertEventFired(PlayerInteractEvent.class);

        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        assertEquals(searchResult.getLocation(), meta.getLodestone());
    }
}
