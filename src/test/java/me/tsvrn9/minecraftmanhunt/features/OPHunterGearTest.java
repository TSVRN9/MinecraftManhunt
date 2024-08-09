package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.BaseTest;
import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OPHunterGearTest extends BaseTest {
    @BeforeEach
    void setup() {
        loadFeatures(new OPHunterGear());
    }

    @Test
    void ensureArmorInCorrectSlot() {
        MinecraftManhunt.giveHunterGear(hunter);

        ItemStack helmet = hunter.getInventory().getHelmet();
        ItemStack chestplate = hunter.getInventory().getChestplate();
        ItemStack leggings = hunter.getInventory().getLeggings();
        ItemStack boots = hunter.getInventory().getBoots();

        assertNotNull(helmet);
        assertNotNull(chestplate);
        assertNotNull(leggings);
        assertNotNull(boots);

        assertTrue(helmet.getType().toString().contains("HELMET"));
        assertTrue(chestplate.getType().toString().contains("CHESTPLATE"));
        assertTrue(leggings.getType().toString().contains("LEGGINGS"));
        assertTrue(boots.getType().toString().contains("BOOTS"));
    }

    @Test
    void ensureItemsOnlyGivenToHunters() {
        MinecraftManhunt.giveRunnerGear(runner);
        MinecraftManhunt.giveHunterGear(hunter);

        ItemStack runnerHelmet = runner.getInventory().getHelmet();
        ItemStack hunterHelmet = hunter.getInventory().getHelmet();

        assertNull(runnerHelmet);
        assertNotNull(hunterHelmet);
    }
}
