package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.plugin.Plugin;

public interface Feature {
    default void onEnable(Plugin plugin) {}

    default void onDisable(Plugin plugin) {}

    default String[] getHandledCommands() {
        return null;
    }

    /**
     * This represents the yaml path where all configuration values for this feature will lie
     */
    String getPath();
}
