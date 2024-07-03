package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoUpdateCompass implements Feature {
    @ConfigValue(value = "frequency_in_ticks")
    private int frequencyInTicks = 20;

    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            Bukkit.getOnlinePlayers().forEach(MinecraftManhunt::updateHunterCompass);
        }
    };

    @Override
    public String getPath() {
        return "auto_update_compass";
    }

    @Override
    public void onEnable(Plugin plugin) {
        task.runTaskTimer(plugin,0, frequencyInTicks);
    }

    @Override
    public void onDisable(Plugin plugin) {
        task.cancel();
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }
}
