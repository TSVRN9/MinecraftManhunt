package me.tsvrn9.minecraftmanhunt;

import me.tsvrn9.minecraftmanhunt.features.*;
import me.tsvrn9.minecraftmanhunt.features.Timer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.StringTemplate.STR;

public class MinecraftManhunt extends JavaPlugin implements Listener {
    private FeatureRegistry featureRegistry;
    private final List<Feature> features = List.of(
        new PrivateChat(),
        new HunterSpeedBuff(),
        new BuffPiglinTrades(),
        new BuffRodDropRate(),
        new PreventBoringDeaths(),
        new AutoUpdateCompass(),
        new RunnerFortressTracking(),
        new OPHunterGear(),
        new Timer(),
        new DisableBoats(),
        new PreventEyesFromBreaking()
    );
    private static ItemStack compass;
    private static final Map<World, Location> lastKnownLocation = new HashMap<>();
    private static Player runner = null;

    public static List<ItemStack> hunterArmor = new ArrayList<>();
    public static List<ItemStack> hunterItems = new ArrayList<>();
    public static List<ItemStack> runnerArmor = new ArrayList<>();
    public static List<ItemStack> runnerItems = new ArrayList<>();

    @Override
    public void onEnable() {
        featureRegistry = new FeatureRegistry(this, features);
        featureRegistry.registerConfigurationSerializables();
        featureRegistry.setConfig(getConfig());

        getServer().getPluginManager().registerEvents(this, this);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        assert compassMeta != null;
        compassMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        compassMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        compassMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        compass.setItemMeta(compassMeta);

        MinecraftManhunt.compass = compass;
    }

    @Override
    public void onDisable() {
        featureRegistry.saveAll();
        featureRegistry.disableAll();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return switch (args.length) {
            case 1 -> {
                String input = args[0].toLowerCase();
                // subcommands
                yield complete(input, "speedrunner", "settings", "save", "reload", "reset");
            }
            case 2 -> switch (args[0].toLowerCase()) {
                case "speedrunner" ->
                        complete(args[1], Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName).toArray(String[]::new));
                case "settings" ->
                        complete(args[1], getConfig().getKeys(true).stream()
                                .filter(k -> !(getConfig().get(k) instanceof MemorySection)).toArray(String[]::new));
                default -> List.of();
            };
            default -> List.of();
        };
    }

    private static List<String> complete(String input, String... possibleCompletions) {
        return Stream.of(possibleCompletions)
                .filter(s -> s.startsWith(input))
                .toList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (runner == null && !(args.length == 2 && args[0].equalsIgnoreCase("speedrunner"))) {
            sender.sendMessage(Component.text("Use /mm speedrunner <name> or click here to set the speedrunner!", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.suggestCommand("/mm speedrunner "))
                    .hoverEvent(HoverEvent.showText(Component.text("/mm speedrunner <name>"))));
            return true;
        }
        if (label.equalsIgnoreCase("mm") || label.equalsIgnoreCase("minecraftmanhunt")) {
            if (args.length == 0) {
                sender.sendMessage(STR."\{ChatColor.RED}Not a valid command!");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "speedrunner" -> {
                    if (args.length == 1) return false;
                    Player player = Bukkit.getPlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage(STR."\{ChatColor.RED}Player not found!");
                        return false;
                    }

                    if (runner == null) {
                        featureRegistry.loadAll();
                    }

                    runner = player;
                    Bukkit.broadcastMessage(STR."\{ChatColor.GREEN}\{player.getName()} is the speedrunner!");
                }
                case "settings" -> {
                    FileConfiguration config = getConfig();
                    switch (args.length) {
                        case 1 -> {
                            for (String key : config.getKeys(true)) {
                                Object value = config.get(key);
                                if (value == null) continue;
                                String stringValue = value.toString();
                                Component component = getComponent(key, stringValue, value);
                                sender.sendMessage(component);
                            }
                        }
                        case 2 -> {
                            String path = args[1];
                            Object value = config.get(path);
                            sender.sendMessage(STR."\"\{path}\"'s value: \{value}");
                        }
                        case 3 -> {
                            String path = args[1];
                            String value = args[2];
                            boolean success = featureRegistry.setValue(path, value);

                            if (success) {
                                sender.sendMessage(STR."\{ChatColor.GREEN}Value updated!");
                                featureRegistry.saveAll();
                            } else {
                                sender.sendMessage(STR."\{ChatColor.RED}Could not update value!");
                                return false;
                            }
                        }
                        default -> {
                            return false;
                        }
                    }
                }
                case "save" -> {
                    sender.sendMessage("Saving config...");
                    featureRegistry.saveAll();
                    sender.sendMessage("Saved config!");
                }
                case "reload" -> {
                    sender.sendMessage("Reloading config...");
                    reload(() -> sender.sendMessage("Reloaded!"));
                }
                case "reset" -> {
                    Bukkit.broadcast(Component.text("Picking a new spot..."));
                    reset();
                }
                default -> {
                    sender.sendMessage(STR."\{ChatColor.RED}Not a valid command!");
                    return false;
                }

            }
        }
        return true;
    }

    private static @NotNull Component getComponent(String key, String stringValue, Object value) {
        String[] lines = stringValue.split("\n");
        Component component = Component.text(key, NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.suggestCommand(STR."/mm settings \{key}"))
                .append(Component.text(":", NamedTextColor.WHITE));

        if (!(value instanceof MemorySection)) {
            Component displayValue = lines.length <= 1
                    ? Component.text(STR." \{stringValue}", NamedTextColor.WHITE)
                    : Component.text(STR." \{lines[0]}", NamedTextColor.WHITE)
                    .hoverEvent(HoverEvent.showText(Component.text(stringValue, NamedTextColor.WHITE)));
            component = component.append(displayValue);
        }
        return component;
    }

    protected void reload(Runnable callback) {
        featureRegistry.disableAll();
        reloadConfig();

        Bukkit.getScheduler().runTask(this, () -> {
            featureRegistry.loadAll();
            featureRegistry.enableAll();
            callback.run();
        });
    }

    private void reset() {
        World world = runner == null ? Bukkit.getWorlds().getFirst() : runner.getWorld();
        Location location = world.getSpawnLocation();
        Set<Biome> bannedBiomes = Set.of(
                Biome.OCEAN,
                Biome.COLD_OCEAN,
                Biome.DEEP_COLD_OCEAN,
                Biome.DEEP_OCEAN,
                Biome.WARM_OCEAN,
                Biome.LUKEWARM_OCEAN,
                Biome.DEEP_LUKEWARM_OCEAN
        );

        location.add(new Vector(50000, 0, 50000));
        while (!bannedBiomes.contains(location.getBlock().getBiome())) {
            location.add(new Vector(500, 0, 500));
        }

        // search for top of the world
        for (int y = 319; y >= 0; y--) {
            location.setY(y);
            Material b = location.getBlock().getType();
            if (b != Material.AIR) {
                break;
            }
        }

        world.setSpawnLocation(location);
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.getInventory().clear();
            p.setRespawnLocation(null, true); // may need to change
            p.setHealth(0);
        });
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke @a everything");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear");
        world.setTime(1000);
    }

    public static boolean isRunner(Player p) { return p.equals(runner); }
    public static boolean isHunter(Player p) { return !p.equals(runner); }
    public static Player getRunner() { return runner; }
    public static void setRunner(Player runner) {
        MinecraftManhunt.runner = runner;
    }

    public static void giveHunterGear(Player p) {
        p.getInventory().addItem(compass);
        p.getInventory().addItem(hunterItems.toArray(new ItemStack[0]));
        if (!hunterArmor.isEmpty()) {
            p.getInventory().setArmorContents(hunterArmor.toArray(new ItemStack[0]));
        }
    }

    public static void giveRunnerGear(Player p) {
        p.getInventory().addItem(runnerItems.toArray(new ItemStack[0]));
        if (!runnerArmor.isEmpty()) {
            p.getInventory().setArmorContents(runnerArmor.toArray(new ItemStack[0]));
        }
    }

    public static boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    public static void giveItems() {
        Bukkit.getOnlinePlayers().stream()
                .filter(MinecraftManhunt::isHunter)
                .forEach(MinecraftManhunt::giveHunterGear);
        giveRunnerGear(runner);
    }

    public static TrackedLocation updateHunterCompass(Player p) {
        if (runner == null) return new TrackedLocation(null, true);

        TrackedLocation trackedLocation = getRunnerLocation(p.getWorld());

        if (p.getInventory().contains(Material.COMPASS)) {
            if (isHunter(p)) {
                if (!trackedLocation.exists()) {
                    return trackedLocation;
                }

                setCompassTarget(p, trackedLocation.location());
            }
        }
        return trackedLocation;
    }

    public static void setCompassTarget(Player p, Location location) {
        World.Environment environment = Objects.requireNonNull(location.getWorld()).getEnvironment();

        if (!p.getInventory().contains(Material.COMPASS)) return;

        ItemStack compass = p.getInventory().getItem(p.getInventory().first(Material.COMPASS));

        if (environment == World.Environment.NORMAL) {
            p.setCompassTarget(location);
            if (compass != null) {
                CompassMeta meta = (CompassMeta) compass.getItemMeta();
                assert meta != null;
                if (meta.getLodestone() != null) {
                    meta.itemName(Component.text("Compass"));
                    meta.setLodestone(null);
                    compass.setItemMeta(meta);
                }
            }
        } else if (environment == World.Environment.NETHER) {
            if (compass == null) return;
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            assert meta != null;
            meta.itemName(Component.text("Compass"));
            meta.setLodestone(location);
            meta.setLodestoneTracked(false);
            compass.setItemMeta(meta);
        }
    }

    public static TrackedLocation getRunnerLocation(World world) {
        if (runner == null) return new TrackedLocation(null, true);

        boolean isOutdated = !runner.getWorld().equals(world);
        Location location = isOutdated ? lastKnownLocation.get(world) : runner.getLocation();

        return new TrackedLocation(location, isOutdated);
    }

    @EventHandler
    public void rightClickCompass(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (runner != null && isRightClickOnCompass(event) && isHunter(p)) {
            TrackedLocation location = updateHunterCompass(p);
            if (location.exists() && !location.isOutdated()) {
                p.sendMessage(STR."\{ChatColor.YELLOW}Updated Compass");
            } else if (location.exists()) {
                p.sendMessage(STR."\{ChatColor.YELLOW}Updated Compass to Last Known Location");
            } else {
                p.sendMessage(STR."\{ChatColor.RED}Speedrunner not found! Compass was not updated");
            }
        }
    }

    public static boolean isRightClickOnCompass(PlayerInteractEvent event) {
        return event.getItem() != null && isRightClick(event.getAction()) && event.getItem().getType() == Material.COMPASS;
    }

    public FeatureRegistry getFeatureRegistry() {
        return featureRegistry;
    }

    public void setFeatureRegistry(List<Feature> features) {
        featureRegistry = new FeatureRegistry(this, features);
    }

    @EventHandler
    public void updateLastKnownLocation(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        if (isRunner(p)) {
            lastKnownLocation.put(event.getFrom().getWorld(), event.getFrom());
        }
    }

    @EventHandler
    public void disconnectEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (isRunner(p)) {
            lastKnownLocation.put(p.getLocation().getWorld(), p.getLocation());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isHunter(player)) {
            giveHunterGear(player);
        } else { // isRunner(player)
            giveRunnerGear(player);
        }
    }
}
