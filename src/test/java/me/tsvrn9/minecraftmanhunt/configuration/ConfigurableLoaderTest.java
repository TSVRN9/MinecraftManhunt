package me.tsvrn9.minecraftmanhunt.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurableLoaderTest {
    YamlConfiguration config;

    SomeConfigurable obj;
    SomeConfigurable clone;
    SomeConfigurable alt;

    @BeforeEach
    public void setup() {
        config = new YamlConfiguration();
        obj = SomeConfigurable.getDefault();
        clone = SomeConfigurable.getDefault();
        alt = SomeConfigurable.getAlternative();
    }

    @Test
    public void loadFromDefaults() {
        ConfigurableLoader.load(obj, config);
        assertEquals(clone, obj, "When given an empty section, values should not be overridden");
    }

    @Test
    public void loadValuesFromConfig() {
        ConfigurableLoader.save(alt, config);
        ConfigurableLoader.load(obj, config);
        assertEquals(alt, obj, "Values should be overridden to match the section");
    }

    @Test
    public void loadSerialized() throws InvalidConfigurationException {
        ConfigurableLoader.save(alt, config);
        String serialized = config.saveToString();

        config = new YamlConfiguration();
        config.loadFromString(serialized);

        ConfigurableLoader.load(obj, config);
        assertEquals(alt, obj, "Values should be overridden to match the section");
    }

    public static class SomeConfigurable {
        @ConfigValue(value = "integer")
        private int i;

        @ConfigValue(value = "string")
        private String helloWorld;

        @ConfigValue(value = "map")
        private Map<String, Integer> map;

        @ConfigValue(value = "list")
        private List<Double> list;

        public SomeConfigurable(int i, String helloWorld, Map<String, Integer> map, List<Double> list) {
            this.i = i;
            this.helloWorld = helloWorld;
            this.map = map;
            this.list = list;
        }

        public static SomeConfigurable getDefault() {
            return new SomeConfigurable(
                    3,
                    "hello world!",
                    Map.of("hello", 2, "world", 3),
                    List.of(3.2 , 5.5, 2.1234, 9.4));
        }

        public static SomeConfigurable getAlternative() {
            return new SomeConfigurable(
                    -33,
                    "goodbye world...",
                    Map.of("brother", -20, "sister", 123),
                    List.of(4.3, 3.2, -3.1234, 4883.2));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SomeConfigurable that)) return false;
            return i == that.i && Objects.equals(helloWorld, that.helloWorld) && Objects.equals(map, that.map) && Objects.equals(list, that.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, helloWorld, map, list);
        }

        @Override
        public String toString() {
            return STR."SomeConfigurable{i=\{i}, helloWorld='\{helloWorld}\{'\''}, map=\{map}, list=\{list}\{'}'}";
        }
    }
}
