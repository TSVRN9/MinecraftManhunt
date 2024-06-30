package me.tsvrn9.minecraftmanhunt.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
    }

    public static void load(Object feature, ConfigurationSection section) {
        Class<?> featureClass = feature.getClass();
        Set<String> keys = section.getKeys(false);
        for (Field field : featureClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);

                    ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                    Class<?> clazz = field.getType();
                    String key = configValue.path();
                    Object unprocessedValue = typeHandlers.containsKey(clazz) && keys.contains(key)
                            ? typeHandlers.get(clazz).apply(section, key)
                            : section.getObject(key, clazz);

                    if (unprocessedValue != null) {
                        Predicate<Object> validator = configValue.validator().getDeclaredConstructor().newInstance();
                        if (!validator.test(unprocessedValue)) {
                            continue;
                        }

                        UnaryOperator<Object> processor = configValue.processor().getDeclaredConstructor().newInstance();
                        Object processedValue = processor.apply(unprocessedValue);

                        field.set(feature, processedValue);
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
                    String key = configValue.path();

                    section.set(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
