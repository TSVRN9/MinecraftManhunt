package me.tsvrn9.minecraftmanhunt.configuration;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

public class ConfigurableLoader {
    private static final Map<Class<?>, BiFunction<ConfigurationSection, String, Object>> typeHandlers = new HashMap<>();

    static {
        typeHandlers.put(int.class, ConfigurationSection::getInt);
        typeHandlers.put(double.class, ConfigurationSection::getDouble);
        typeHandlers.put(boolean.class, ConfigurationSection::getBoolean);
        typeHandlers.put(long.class, ConfigurationSection::getLong);
        typeHandlers.put(float.class, (section, key) -> (float) section.getDouble(key));
        typeHandlers.put(byte.class, (section, key) -> (byte) section.getInt(key));
        typeHandlers.put(short.class, (section, key) -> (short) section.getInt(key));
        typeHandlers.put(char.class, (section, key) -> Objects.requireNonNull(section.getString(key)).charAt(0));
        typeHandlers.put(Map.class, (section, key) -> {
            HashMap<Object, Object> hashMap = new HashMap<>();
            ConfigurationSection configurationMap = section.getConfigurationSection(key);
            if (configurationMap != null) {
                configurationMap.getKeys(false).forEach(k -> hashMap.put(k, configurationMap.get(k)));
            } else {
                hashMap.putAll((Map<?, ?>) section.get(key));
            }
            return hashMap;
        });
    }

    // NOT DEEP
    public static void load(Object feature, ConfigurationSection section) {
        Class<?> featureClass = feature.getClass();
        Set<String> keys = section.getKeys(false);
        for (Field field : featureClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);

                    ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                    Class<?> clazz = field.getType();
                    String key = configValue.value();

                    if (keys.contains(key)) {
                        Object value = typeHandlers.containsKey(clazz)
                                ? typeHandlers.get(clazz).apply(section, key)
                                : section.getObject(key, clazz);

                        field.set(feature, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void save(Object feature, ConfigurationSection section) {
        Class<?> featureClass = feature.getClass();
        for (Field field : featureClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);
                    ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                    Object value = field.get(feature);
                    String key = configValue.value();

                    section.set(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
