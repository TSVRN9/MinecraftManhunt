package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class BuffRodDropRate implements Feature, Listener {
    @ConfigValue(path="rod_drop_probability")
    private double rodDropProbability = .75;

    @Override
    public String getPath() {
        return "buff_rod_drop_rate";
    }

    @EventHandler
    public void buffBlazes(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.BLAZE) {
            event.getDrops().clear();
            if (Math.random() < rodDropProbability)
                event.getDrops().add(new ItemStack(Material.BLAZE_ROD));
        }
    }
}
