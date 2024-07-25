// These tests were modified from AI generated code
package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private YamlConfiguration config;

    @BeforeEach
    void setUp() throws IOException, InvalidConfigurationException {
        server = MockBukkit.mock();
        plugin = getMockPlugin();
        mockFeature = mock(Feature.class, withSettings().extraInterfaces(CommandExecutor.class, TabCompleter.class));
        config = new YamlConfiguration();

        File file = new File(plugin.getDataFolder(), "config.yml");

        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();

        config.load(file);

        when(mockFeature.getPath()).thenReturn("mockFeature");
        when(mockFeature.getHandledCommands()).thenReturn(new String[]{"mock_command"});
        when(mockFeature.enabledByDefault()).thenReturn(true);
        when(((CommandExecutor) mockFeature).onCommand(any(), any(), any(), any())).thenReturn(true);

        featureRegistry = new FeatureRegistry(plugin, List.of(mockFeature)).setConfig(config);
    }

    private JavaPlugin getMockPlugin() {
        JavaPlugin plugin = null;
        try (InputStream mockYaml = FeatureRegistryTest.class.getClassLoader().getResourceAsStream("mock_plugin.yml")) {
            PluginDescriptionFile description = new PluginDescriptionFile(mockYaml);
            plugin = server.getPluginManager().loadPlugin(MockPlugin.class, description, new Object[0]);
            server.getPluginManager().enablePlugin(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plugin;
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
    void testLoadAllFeatures() {
        featureRegistry.loadAll();

        boolean isEnabled = config.getConfigurationSection(mockFeature.getPath()).getBoolean("enabled");
        assertEquals(isEnabled, featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testEnableFeature() {
        featureRegistry.enable(mockFeature);

        verify(mockFeature).onEnable(plugin);
        assertTrue(featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testDisableFeature() {
        featureRegistry.enable(mockFeature);
        featureRegistry.disable(mockFeature);

        verify(mockFeature).onDisable(plugin);
        assertFalse(featureRegistry.isEnabled.get(mockFeature));
    }

    @Test
    void testOnCommand() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("mock_command");

        featureRegistry.enable(mockFeature);

        assertTrue(featureRegistry.onCommand(sender, command, "mockCommand", new String[0]));
        verify((CommandExecutor) mockFeature).onCommand(sender, command, "mockCommand", new String[0]);
    }

    @Test
    void testOnTabComplete() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("mock_command");

        featureRegistry.enable(mockFeature);

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
