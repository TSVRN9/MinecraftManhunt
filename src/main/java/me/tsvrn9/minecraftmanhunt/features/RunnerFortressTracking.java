package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.generator.structure.Structure;

import java.util.Objects;

public class RunnerFortressTracking implements Feature, Listener {
    @Override
    public String getPath() {
        return "runner_can_track_fortress";
    }

    @EventHandler
    private void onCompass(PlayerInteractEvent event){
        if (!MinecraftManhunt.isRightClickOnCompass(event)) return;

        Player p = event.getPlayer();

        if (MinecraftManhunt.isRunner(p) && p.getWorld().getEnvironment() == World.Environment.NETHER) {
            World nether = p.getWorld();
            Location result = Objects.requireNonNull(nether.locateNearestStructure(p.getLocation(), Structure.FORTRESS, 750, false)).getLocation();

            MinecraftManhunt.setCompassTarget(p, result);
            p.sendMessage(STR."\{ChatColor.YELLOW}Updated Compass to Closest Nether Fortress");
        }
    }

}
