package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class DisableBoats implements Feature, Listener {
    @Override
    public String getPath() {
        return "disable_boats";
    }

    @EventHandler
    public void onBoatPlace(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.BOAT
                || e.getEntityType() == EntityType.CHEST_BOAT) {
            e.setCancelled(true);
        }
    }
}
