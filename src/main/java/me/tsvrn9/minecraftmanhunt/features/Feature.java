package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.plugin.Plugin;

public interface Feature {
    default void onEnable(Plugin plugin) {}

    default void onDisable(Plugin plugin) {}

    default String[] getHandledCommands() {
        return null;
    }

    default Class<?>[] getConfigurationSerializables() {
        return null;
    }

    default boolean enabledByDefault() { return false; }

    /**
     * This represents the yaml value where all configuration values for this feature will lie
     */
    String getPath();
}
