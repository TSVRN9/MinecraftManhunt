package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Features implements CommandExecutor, TabCompleter {
    private Features() {}

    private static final Features SINGLETON = new Features();

    private static final Map<String, Feature> pathToFeature = new HashMap<>();
    private static final Map<String, Feature> commandRegistry = new HashMap<>();
    private static final Map<String, Feature> tabRegistry = new HashMap<>();
    private static final Map<Feature, Boolean> isEnabled = new HashMap<>();
    private static final Map<Class<?>, Function<String, Object>> stringToObjectHandlers = new HashMap<>();

    static {
        stringToObjectHandlers.put(int.class, Integer::parseInt);
        stringToObjectHandlers.put(double.class, Double::parseDouble);
        stringToObjectHandlers.put(boolean.class, Boolean::parseBoolean);
        stringToObjectHandlers.put(long.class, Long::parseLong);
        stringToObjectHandlers.put(float.class, Float::parseFloat);
        stringToObjectHandlers.put(byte.class, Byte::parseByte);
        stringToObjectHandlers.put(short.class, Short::parseShort);
        stringToObjectHandlers.put(char.class, s -> s.charAt(0));
        stringToObjectHandlers.put(String.class, s -> s);
    }

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
            isEnabled.put(feature, false);
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

    public static void loadAll(JavaPlugin plugin) {
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

    public static void saveAll(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        for (Feature feature : FEATURES) {
            ConfigurationSection section = config.getConfigurationSection(feature.getPath());
            if (section == null) {
                section = config.createSection(feature.getPath());
            }
            ConfigurableLoader.save(feature, section);
            section.set("enabled", isEnabled.get(feature));
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

    public static void reloadAll(JavaPlugin plugin) {
        for (Feature feature : FEATURES) {
            reload(feature, plugin);
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

    public static void reload(Feature feature, JavaPlugin plugin) {
        boolean wasEnabled = isEnabled.get(feature);
        disable(feature, plugin);

        if (wasEnabled)
            enable(feature, plugin);
    }

    public static Feature getFeature(String path) {
        return pathToFeature.get(path);
    }

    public static boolean setValue(String path, String value, JavaPlugin plugin) {
        if (path.indexOf('.') == -1) return false;
        String featurePath = path.substring(0, path.indexOf('.'));
        String valuePath = path.substring(path.indexOf('.') + 1);
        Feature feature = getFeature(featurePath);

        if (feature == null) return false;

        Class<? extends Feature> cls = feature.getClass();
        for (Field field : cls.getFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);
                    ConfigValue configValue = field.getAnnotation(ConfigValue.class);
                    String key = configValue.value();

                    if (key.equals(valuePath)) {
                        if (stringToObjectHandlers.containsKey(field.getType())) { // only support primitives && strings
                            if (isEnabled.get(feature)) {
                                feature.onDisable(plugin);
                            }
                            Object newValue = stringToObjectHandlers.get(field.getType()).apply(value);
                            field.set(feature, newValue);
                            if (isEnabled.get(feature)) {
                                feature.onEnable(plugin);
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
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