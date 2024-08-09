package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class DisableBoats implements Feature, Listener {
    @Override
    public String getPath() {
        return "disable_boats";
    }

    @EventHandler
    public void onBoatPlace(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof Boat) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("Boats are disabled!");
        }
    }
}
