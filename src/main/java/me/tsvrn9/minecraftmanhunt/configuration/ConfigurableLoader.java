package me.tsvrn9.minecraftmanhunt.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ConfigurableLoader {
    public static void load(Object feature, ConfigurationSection section) {
        Class<?> featureClass = feature.getClass();
        for (Field field : featureClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                try {
                    field.setAccessible(true);
                    if (ConfigurationSerializable.class.isAssignableFrom(field.getType())) {
                        ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                        @SuppressWarnings("unchecked") // We just checked the type above...
                        Class<? extends ConfigurationSerializable> clazz = (Class<? extends ConfigurationSerializable>) field.getType();

                        String key = configValue.path();
                        Object serializedValue = section.getSerializable(key, clazz);

                        if (serializedValue != null) {
                            Predicate<Object> validator = configValue.validator().getDeclaredConstructor().newInstance();
                            if (!validator.test(serializedValue)) {
                                continue;
                            }

                            UnaryOperator<Object> processor = configValue.processor().getDeclaredConstructor().newInstance();
                            Object processedValue = processor.apply(serializedValue);

                            field.set(feature, processedValue);
                        }
                    } else {
                        throw new IllegalArgumentException("Any fields using @ConfigValue must implement ConfigurationSerializable");
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
                    if (ConfigurationSerializable.class.isAssignableFrom(field.getType())) {
                        ConfigValue configValue = field.getAnnotation(ConfigValue.class);

                        @SuppressWarnings("unchecked")
                        Class<? extends ConfigurationSerializable> clazz = (Class<? extends ConfigurationSerializable>) field.getType();

                        ConfigurationSerializable value = (ConfigurationSerializable) field.get(feature);
                        String key = configValue.path();

                        section.set(key, value);
                    } else {
                        throw new IllegalArgumentException("Any fields using @ConfigValue must implement ConfigurationSerializable");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
