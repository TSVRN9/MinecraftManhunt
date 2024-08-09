package me.tsvrn9.minecraftmanhunt.features;

import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class PreventEyesFromBreaking implements Feature, Listener {
    @Override
    public String getPath() {
        return "prevent_eyes_from_breaking";
    }

    @EventHandler
    public void preventEyesFromBreaking(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EYE_OF_ENDER) {
            if (event.getEntity() instanceof EnderSignal enderSignal) {
                enderSignal.setDropItem(true);
            } else {
                throw new IllegalStateException("EntityType.EYE_OF_ENDER is not an EnderSignal");
            }
        }
    }
}
