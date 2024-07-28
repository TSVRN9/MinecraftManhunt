package me.tsvrn9.minecraftmanhunt.features;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateChat implements Feature, CommandExecutor, TabCompleter, Listener {
    private final Map<Player, Boolean> usingPrivateChannel = new HashMap<>();

    @ConfigValue(value = "enabled_on_join")
    private boolean enabledOnJoin = true;

    @Override
    public String getPath() {
        return "private_chat";
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (usingPrivateChannel.getOrDefault(event.getPlayer(), enabledOnJoin) && MinecraftManhunt.isHunter(event.getPlayer())) {
            event.viewers().remove(MinecraftManhunt.getRunner());
            event.message(STR."\{ChatColor.LIGHT_PURPLE}Private:\{ChatColor.RESET} <%s> %s"); // TODO MIGRATE TO ADVENTURE API
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

    @Override
    public String[] getHandledCommands() {
        return new String[] { "shout", "privatechat" };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "shout" -> {
                if (args.length == 0 && sender instanceof Player player) {
                    boolean isEnabled = !usingPrivateChannel.get(player);

                    sender.sendMessage(STR."\{ChatColor.GREEN}\{isEnabled ? "Enabled" : "Disabled"} Private Chat");
                    usingPrivateChannel.put(player, isEnabled);
                } else {
                    String message = STR."<\{sender.getName()}> \{Arrays.stream(args).reduce((total, el) -> STR."\{total} \{el}")}";
                    Bukkit.broadcastMessage(message);
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
