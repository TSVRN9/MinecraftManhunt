package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class PrivateChat implements Feature {
    private final Map<Player, Boolean> usingPrivateChannel = new HashMap<>();
    private boolean defaultValue;

    @Override
    public void onLoad(ConfigurationSection section) {
        defaultValue = section.get("default_value");
    }

    @Override
    public String getPath() {
        return "privatechat";
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
        event.getPlayer()
    }
}
