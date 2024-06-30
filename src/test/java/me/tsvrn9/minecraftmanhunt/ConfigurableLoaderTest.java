package me.tsvrn9.minecraftmanhunt;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigurableLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurableLoaderTest {
    SomeConfigurable obj;
    SomeConfigurable clone;
    ConfigurationSection config;

    @BeforeEach
    public void setup() {
        config = new YamlConfiguration();
        obj = SomeConfigurable.getDefault();
        clone = SomeConfigurable.getDefault();
    }

    @Test
    public void testLoadFromDefaults() {
        ConfigurableLoader.load(obj, config);
        assertEquals(obj, clone, "When given an empty config, values should not be overriden");
    }

    private record SomeConfigurable(
            @ConfigValue(path = "integer") int i,
            @ConfigValue(path = "string") String helloWorld
    ) {
        private static SomeConfigurable getDefault() {
            return new SomeConfigurable(3, "hello world!");
        }
    }
}
