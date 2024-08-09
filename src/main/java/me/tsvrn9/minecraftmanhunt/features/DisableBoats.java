package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.Plugin;

public class DisableBoats implements Feature, Listener {
    private Plugin plugin;

    @Override
    public String getPath() {
        return "disable_boats";
    }

    @Override
    public void onEnable(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBoatPlace(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof Boat) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("Boats are disabled!");

            Bukkit.getScheduler().runTask(plugin, () -> e.getRightClicked().eject());
        }
    }
}
