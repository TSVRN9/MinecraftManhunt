package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface Feature extends Listener {
    default void onEnable(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    default void onDisable(Plugin plugin) {
        HandlerList.unregisterAll(this);
    }

    default String[] getHandledCommands() {
        return null;
    }

    /**
     * This represents the yaml path where all configuration values for this feature will lie
     */
    String getPath();
}
