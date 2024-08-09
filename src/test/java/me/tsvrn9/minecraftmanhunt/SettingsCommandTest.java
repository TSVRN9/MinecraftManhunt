package me.tsvrn9.minecraftmanhunt;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import me.tsvrn9.minecraftmanhunt.features.Feature;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SettingsCommandTest extends BaseTest {
    SomeFeature someFeature;
    @BeforeEach
    void setup() {
        someFeature = spy(new SomeFeature());
        loadFeatures(someFeature);
    }

    @Test
    void testEnableCommand() {
        Bukkit.dispatchCommand(hunter, STR."mm settings \{someFeature.getPath()}.enabled true");

        assertTrue(manhunt.getFeatureRegistry().isEnabled(someFeature));
        verify(someFeature, times(2)).onEnable(any()); // loadFeatures(...) enables the feature once
    }

    @Test
    void testDisableCommand() {
        Bukkit.dispatchCommand(hunter, STR."mm settings \{someFeature.getPath()}.enabled false");

        assertFalse(manhunt.getFeatureRegistry().isEnabled(someFeature));
        verify(someFeature).onDisable(any());
    }

    @Test
    void testChangeValue() {
        Bukkit.dispatchCommand(hunter, STR."mm settings \{someFeature.getPath()}.some_value hello");

        assertTrue(manhunt.getFeatureRegistry().isEnabled(someFeature));
        assertEquals("hello", someFeature.someValue);
    }

    public class SomeFeature implements Feature {
        final static String SOME_VALUE = "someValue";

        @ConfigValue("some_value")
        String someValue = SOME_VALUE;

        @Override
        public String getPath() {
            return "somefeature";
        }
    }
}
