// These tests were modified from AI generated code
package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeatureRegistryTest {
    private ServerMock server;
    private JavaPlugin plugin;
    private FeatureRegistry featureRegistry;
    private Feature mockFeature;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        mockFeature = mock(Feature.class);
        when(mockFeature.getPath()).thenReturn("mockFeature");
        when(mockFeature.getHandledCommands()).thenReturn(new String[]{"mockCommand"});
        when(mockFeature.enabledByDefault()).thenReturn(true);
        featureRegistry = new FeatureRegistry(List.of(mockFeature));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testRegisterConfigurationSerializables() {
        Class<?>[] serializables = {MockSerializable.class};
        when(mockFeature.getConfigurationSerializables()).thenReturn(serializables);

        featureRegistry.registerConfigurationSerializables();

        // Since ConfigurationSerialization is a static method, this test might need PowerMockito or other means to verify
        // Verify that ConfigurationSerialization.registerClass was called
        verify(mockFeature).getConfigurationSerializables();
    }

    @Test
    void testLoadAllFeatures() throws IOException, InvalidConfigurationException {
        FileConfiguration config = new YamlConfiguration();
        config.load(new File(plugin.getDataFolder(), "config.yml"));
        when(plugin.getConfig()).thenReturn(config);

        featureRegistry.loadAll(plugin);

        boolean isEnabled = config.getConfigurationSection(mockFeature.getPath()).getBoolean("enabled");
        assertEquals(isEnabled, featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testEnableFeature() {
        featureRegistry.enable(mockFeature, plugin);

        verify(mockFeature).onEnable(plugin);
        assertTrue(featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testDisableFeature() {
        featureRegistry.enable(mockFeature, plugin);
        featureRegistry.disable(mockFeature, plugin);

        verify(mockFeature).onDisable(plugin);
        assertFalse(featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testOnCommand() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("mockCommand");

        featureRegistry.enable(mockFeature, plugin);

        assertTrue(featureRegistry.onCommand(sender, command, "mockCommand", new String[0]));
        verify((CommandExecutor) mockFeature).onCommand(sender, command, "mockCommand", new String[0]);
    }

    @Test
    void testOnTabComplete() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("mockCommand");

        featureRegistry.enable(mockFeature, plugin);

        featureRegistry.onTabComplete(sender, command, "mockCommand", new String[0]);
        verify((TabCompleter) mockFeature).onTabComplete(sender, command, "mockCommand", new String[0]);
    }



    @SerializableAs("MockSerializable")
    private static class MockSerializable implements ConfigurationSerializable {
        private String name;
        private int value;

        public MockSerializable(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("value", value);
            return map;
        }

        public static MockSerializable deserialize(Map<String, Object> args) {
            String name = (String) args.get("name");
            int value = (Integer) args.get("value");
            return new MockSerializable(name, value);
        }

        @Override
        public String toString() {
            return STR."MockSerializable{name='\{name}\{'\''}, value=\{value}\{'}'}";
        }
    }
}
