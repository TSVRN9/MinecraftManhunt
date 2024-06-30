package me.tsvrn9.minecraftmanhunt.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ConfigurableLoader {
    public static void load(Object feature, ConfigurationSection section) {
        Class<?> featureClass = feature.getClass();
        for (Field field : featureClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);

                    ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                    Class<?> clazz = field.getType();
                    String key = configValue.path();
                    Object unprocessedValue = section.getObject(key, clazz);

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

                    ConfigurationSerializable value = (ConfigurationSerializable) field.get(feature);
                    String key = configValue.path();

                    section.set(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
