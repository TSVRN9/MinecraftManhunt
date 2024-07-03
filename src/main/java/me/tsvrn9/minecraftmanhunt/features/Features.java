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
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Features implements CommandExecutor, TabCompleter {
    private Features() {}

    private static final Features SINGLETON = new Features();
    private static final Map<String, Feature> pathToFeature = new HashMap<>();
    private static final Map<String, Feature> commandRegistry = new HashMap<>();
    private static final Map<String, Feature> tabRegistry = new HashMap<>();
    private static final Map<Feature, Boolean> isEnabled = new HashMap<>();

    public static final List<Feature> FEATURES = List.of(
            new PrivateChat(),
            new HunterSpeedBuff(),
            new BuffPiglinTrades(),
            new BuffRodDropRate(),
            new PreventBoringDeaths(),
            new AutoUpdateCompass(),
            new RunnerFortressTracking(),
            new OPHunterGear(),
            new Timer()
    );

    static {
        // init pathToFeature map
        for (Feature feature : FEATURES) {
            pathToFeature.put(feature.getPath(), feature);
        }
    }

    public static void registerConfigurationSerializables() {
        for (Feature feature : FEATURES) {
            Class<?>[] serializables = feature.getConfigurationSerializables();
            if (serializables != null) {
                for (Class<?> serializable : serializables) {
                    if (ConfigurationSerializable.class.isAssignableFrom(serializable)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends ConfigurationSerializable> clazz = (Class<? extends ConfigurationSerializable>) serializable;
                        ConfigurationSerialization.registerClass(clazz, serializable.getSimpleName());
                    } else {
                        throw new IllegalStateException(STR."Class \{serializable.getName()} is not a ConfigurationSerializable");
                    }
                }
            }
        }
    }

    public static void load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        for (Feature feature : FEATURES) {
            ConfigurationSection section = config.getConfigurationSection(feature.getPath());
            if ((section == null && feature.enabledByDefault()) || (section != null && section.getBoolean("enabled"))) {
                enable(feature, plugin);
            } else {
                isEnabled.put(feature, false);
            }
        }
    }

    public static void save(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        for (Feature feature : FEATURES) {
            boolean wasEnabled = isEnabled.get(feature);
            disable(feature, plugin);
            ConfigurationSection section = config.getConfigurationSection(feature.getPath());
            ConfigurableLoader.save(feature, section);
            assert section != null;
            section.set("enabled", wasEnabled);
        }
        plugin.saveConfig();
    }

    public static void enableAll(JavaPlugin plugin) {
        for (Feature feature : FEATURES) {
            enable(feature, plugin);
        }
    }

    public static void disableAll(JavaPlugin plugin) {
        for (Feature feature : FEATURES) {
            disable(feature, plugin);
        }
    }

    public static void enable(Feature feature, JavaPlugin plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(feature.getPath());

        if (section != null) {
            ConfigurableLoader.load(feature, section);
        }

        String[] handledCommands = feature.getHandledCommands();

        if (handledCommands != null && handledCommands.length != 0) {
            if (feature instanceof CommandExecutor) {
                for (String command : handledCommands) {
                    command = command.toLowerCase();
                    Objects.requireNonNull(plugin.getCommand(command)).setExecutor(SINGLETON);
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
                    Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(SINGLETON);
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
        feature.onDisable(plugin);

        if (feature instanceof Listener listener) {
            HandlerList.unregisterAll(listener);
        }

        isEnabled.put(feature, false);
    }

    public static Feature getFeature(String path) {
        return pathToFeature.get(path);
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