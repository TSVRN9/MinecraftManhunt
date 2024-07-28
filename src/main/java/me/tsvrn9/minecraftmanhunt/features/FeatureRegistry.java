package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigurableLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
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

public class FeatureRegistry implements CommandExecutor, TabCompleter {
    private static final Map<Class<?>, Function<String, Object>> stringToObjectHandlers = new HashMap<>();

    protected final Map<String, Feature> pathToFeature = new HashMap<>();
    protected final Map<String, Feature> commandRegistry = new HashMap<>();
    protected final Map<String, Feature> tabRegistry = new HashMap<>();
    protected final Map<Feature, Boolean> isEnabled = new HashMap<>();

    protected final JavaPlugin plugin;
    protected ConfigurationSection config;
    protected final List<Feature> features;

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

    public FeatureRegistry(JavaPlugin plugin, List<Feature> features) {
        this.plugin = plugin;
        // init pathToFeature map
        for (Feature feature : features) {
            pathToFeature.put(feature.getPath(), feature);
            isEnabled.put(feature, false);
        }
        this.features = features;
    }

    public FeatureRegistry setConfig(ConfigurationSection config) {
        this.config = config;
        return this;
    }

    public void registerConfigurationSerializables() {
        for (Feature feature : features) {
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

    public void loadAll() {
        for (Feature feature : features) {
            ConfigurationSection section = config.getConfigurationSection(feature.getPath());
            boolean firstTimeLoaded = section == null;
            if ((firstTimeLoaded && feature.enabledByDefault()) || (!firstTimeLoaded && section.getBoolean("enabled"))) {
                enable(feature);
            } else {
                isEnabled.put(feature, false);
            }

            if (firstTimeLoaded) {
                save(feature);
            }
        }
    }

    public void saveAll() {
        for (Feature feature : features) {
            save(feature);
        }
        plugin.saveConfig();
    }

    public void save(Feature feature) {
        ConfigurationSection section = config.getConfigurationSection(feature.getPath());
        if (section == null) {
            section = config.createSection(feature.getPath());
        }
        ConfigurableLoader.save(feature, section);
        section.set("enabled", isEnabled.get(feature));
    }

    public void enableAll() {
        for (Feature feature : features) {
            enable(feature);
        }
    }

    public void disableAll() {
        for (Feature feature : features) {
            if (isEnabled.get(feature))
                disable(feature);
        }
    }

    public void reloadAll() {
        for (Feature feature : features) {
            reload(feature);
        }
    }

    public void enable(Feature feature) {
        ConfigurationSection section = config.getConfigurationSection(feature.getPath());

        if (section != null) {
            ConfigurableLoader.load(feature, section);
        }

        String[] handledCommands = feature.getHandledCommands();

        if (handledCommands != null && handledCommands.length != 0) {
            if (feature instanceof CommandExecutor) {
                for (String commandName : handledCommands) {
                    commandName = commandName.toLowerCase();
                    PluginCommand command = plugin.getCommand(commandName);
                    if (command == null) {
                        throw new IllegalStateException(STR."Command \{commandName} not found");
                    }
                    command.setExecutor(this);
                    commandRegistry.put(commandName, feature);
                }
            } else {
                throw new IllegalArgumentException(
                        STR."Feature with \"\{feature.getPath()}\" is attempting to handle a command but does not implement CommandExecutor!"
                );
            }
            if (feature instanceof TabCompleter) {
                for (String command : handledCommands) {
                    command = command.toLowerCase();
                    Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(this);
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

    public void disable(Feature feature) {
        feature.onDisable(plugin);

        if (feature instanceof Listener listener) {
            HandlerList.unregisterAll(listener);
        }

        isEnabled.put(feature, false);
    }

    public void reload(Feature feature) {
        boolean wasEnabled = isEnabled.get(feature);
        disable(feature);

        if (wasEnabled)
            enable(feature);
    }

    public Feature getFeature(String path) {
        return pathToFeature.get(path);
    }

    public boolean setValue(String path, String value) {
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