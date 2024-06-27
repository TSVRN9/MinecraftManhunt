package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PrivateChat implements Feature {
    private final Map<Player, Boolean> usingPrivateChannel = new HashMap<>();

    @ConfigValue("enabled_on_join")
    private boolean enabledOnJoin = true;

    @Override
    public String getPath() {
        return "private_chat";
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (usingPrivateChannel.get(event.getPlayer()) && MinecraftManhunt.isHunter(event.getPlayer())) {
            event.getRecipients().remove(MinecraftManhunt.getRunner());
            event.setFormat(STR."\{ChatColor.LIGHT_PURPLE}Private:\{ChatColor.RESET} <%s> %s");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        usingPrivateChannel.put(p, enabledOnJoin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        usingPrivateChannel.remove(p);
    }
}
