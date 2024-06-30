package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;

public class BuffPiglinTrades implements Feature, Listener {
    @ConfigValue(path="pearl_probability")
    private double pearlProbability = .2;

    @Override
    public String getPath() {
        return "buff_piglin_trades";
    }

    @EventHandler
    public void buffTrades(PiglinBarterEvent event) {
        if (Math.random() < pearlProbability) {
            event.getOutcome().add(new ItemStack(Material.ENDER_PEARL, (int) Math.ceil(Math.random() * 2)));
        }
    }
}
