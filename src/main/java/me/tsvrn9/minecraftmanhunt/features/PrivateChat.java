package me.tsvrn9.minecraftmanhunt.features;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateChat implements Feature, CommandExecutor, TabCompleter, Listener {
    private final Map<Player, Boolean> usingPrivateChannel = new HashMap<>();
    private final ChatRenderer renderer = ChatRenderer.viewerUnaware(
            (_, sourceDisplayName, message) -> Component.text("Private: ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(STR."<\{sourceDisplayName}> "))
                    .append(message)
    );

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
            event.renderer(renderer);
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "shout" -> {
                if (args.length == 0 && sender instanceof Player player) {
                    boolean isEnabled = !usingPrivateChannel.get(player);

                    sender.sendMessage(STR."\{ChatColor.GREEN}\{isEnabled ? "Enabled" : "Disabled"} Private Chat");
                    usingPrivateChannel.put(player, isEnabled);
                } else {
                    String message = STR."<\{sender.getName()}> \{Arrays.stream(args).reduce((total, el) -> STR."\{total} \{el}")}";
                    Bukkit.broadcast(Component.text(message));
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        return List.of();
    }
}
