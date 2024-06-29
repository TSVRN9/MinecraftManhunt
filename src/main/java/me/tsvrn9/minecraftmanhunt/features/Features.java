package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigurableLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Features implements CommandExecutor, TabCompleter {
    private Features() {}

    private static Features SINGLETON = new Features();
    private static Map<String, Feature> commandRegistry = new HashMap<>();
    private static Map<String, Feature> tabRegistry = new HashMap<>();
    private static Map<Feature, Boolean> isEnabled = new HashMap<>();

    public static final List<Feature> FEATURES = List.of(
            new PrivateChat(),
            new HunterSpeedBuff()
    );

    public static void enableAll(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        for (Feature feature : FEATURES) {
            enable(feature, plugin, config);
        }
    }

    public static void disableAll(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        for (Feature feature : FEATURES) {
            disable(feature, plugin, false);
        }
        plugin.saveConfig();
    }

    public static void enable(Feature feature, JavaPlugin plugin, FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection(feature.getPath());

        if (section != null) {
            ConfigurableLoader.load(feature, section);
        }

        String[] handledCommands = feature.getHandledCommands();

        if (handledCommands != null && handledCommands.length != 0) {
            if (feature instanceof CommandExecutor) {
                for (String command : handledCommands) {
                    command = command.toLowerCase();
                    plugin.getCommand(command).setExecutor(SINGLETON);
                    commandRegistry.put(command, feature);
                }
            } else {
                throw new IllegalArgumentException(
                        STR."Feature with \"\{feature.getPath()}\" is attempting to handle a command but does not implement CommandExecutor!"
                );
            }
            if (feature instanceof TabCompleter) {
                for (String command : handledCommands) {
                    command = command.toLowerCase();
                    plugin.getCommand(command).setTabCompleter(SINGLETON);
                    tabRegistry.put(command, feature);
                }
            }
        }

        if (feature instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

        feature.onEnable(plugin);

        isEnabled.put(feature, true);
    }

    public static void disable(Feature feature, JavaPlugin plugin) {
        disable(feature, plugin, true);
    }

    public static void disable(Feature feature, JavaPlugin plugin, boolean save) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(feature.getPath());

        if (section == null) {
            section = plugin.getConfig().createSection(feature.getPath());
        }

        ConfigurableLoader.save(feature, section);
        feature.onDisable(plugin);

        isEnabled.put(feature, false);

        if (save) plugin.saveConfig();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        if (commandRegistry.containsKey(commandName)) {
            Feature feature = commandRegistry.get(commandName);
            if (isEnabled.get(feature)) {
                if (feature instanceof CommandExecutor executor) {
                    executor.onCommand(sender, command, label, args);
                } else {
                    throw new IllegalArgumentException(
                            STR."Feature \"\{feature.getPath()}\" is attempting to handle a command but does not implement CommandExecutor!"
                    );
                }
            } else {
                sender.sendMessage(STR."\{ChatColor.RED}This command has been disabled!");
            }
            return true;
        } else {
            throw new IllegalStateException("Command registry doesn't contain this command, yet onCommand ran?");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        if (tabRegistry.containsKey(commandName)) {
            Feature feature = tabRegistry.get(commandName);
            if (isEnabled.get(feature)) {
                if (feature instanceof TabCompleter completer) {
                    return completer.onTabComplete(sender, command, label, args);
                } else {
                    throw new IllegalArgumentException(
                            STR."Feature \"\{feature.getPath()}\" is attempting to complete a command but does not implement TabCompleter!"
                    );
                }
            } else {
                return List.of();
            }
        } else {
            throw new IllegalStateException("Tab registry doesn't contain this command, yet onTabComplete ran?");
        }

    }
}