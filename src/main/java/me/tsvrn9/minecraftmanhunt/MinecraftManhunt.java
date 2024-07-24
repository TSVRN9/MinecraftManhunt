package me.tsvrn9.minecraftmanhunt;

import me.tsvrn9.minecraftmanhunt.features.*;
import me.tsvrn9.minecraftmanhunt.features.Timer;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.StringTemplate.STR;

public final class MinecraftManhunt extends JavaPlugin implements Listener {
    public static final List<String> REMOVE_ON_DEATH_LORE = List.of(STR."\{ChatColor.COLOR_CHAR}O");

    private static FeatureRegistry featureRegistry = new FeatureRegistry(
            List.of(
                new PrivateChat(),
                new HunterSpeedBuff(),
                new BuffPiglinTrades(),
                new BuffRodDropRate(),
                new PreventBoringDeaths(),
                new AutoUpdateCompass(),
                new RunnerFortressTracking(),
                new OPHunterGear(),
                new Timer()
            )
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
        getServer().getPluginManager().registerEvents(this, this);
        featureRegistry.registerConfigurationSerializables();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        assert compassMeta != null;
        compassMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        compassMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        compassMeta.setLore(REMOVE_ON_DEATH_LORE);
        compass.setItemMeta(compassMeta);

        MinecraftManhunt.compass = compass;
    }

    @Override
    public void onDisable() {
        featureRegistry.saveAll(this);
        featureRegistry.disableAll(this);
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
                        complete(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
                case "settings" -> // TODO pushed to 3.0
                        List.of();
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
                        featureRegistry.loadAll(this);
                    }

                    runner = player;
                    Bukkit.broadcastMessage(STR."\{ChatColor.GREEN}\{player.getName()} is the speedrunner!");
                }
                case "settings" -> {
                    FileConfiguration config = getConfig();
                    switch (args.length) {
                        case 1 -> {
                            String path = args[0];
                            Object value = config.get(path);
                            sender.sendMessage(STR."\"\{path}\"'s value: \{value}");
                        }
                        case 2 -> {
                            String path = args[0];
                            String value = args[1];
                            boolean success = featureRegistry.setValue(path, value, this);

                            if (success) {
                                sender.sendMessage(STR."\{ChatColor.GREEN}Value updated!");
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
                case "save" -> saveConfig();
                case "reload" -> {
                    featureRegistry.disableAll(this);
                    reloadConfig();
                    featureRegistry.enableAll(this);
                }
                case "reset" -> reset();
                default -> {
                    sender.sendMessage(STR."\{ChatColor.RED}Not a valid command!");
                    return false;
                }

            }
        }
        return true;
    }

    private void reset() {
        World world = Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                .findFirst().orElse(runner.getWorld());
        Location location = world.getSpawnLocation();
        location.add(new Vector(50000, 0, 50000));

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
            p.setHealth(0);
        });
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke @a everything");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather set clear");
        world.setTime(1000);
    }

    public static boolean isRunner(Player p) { return p.equals(runner); }
    public static boolean isHunter(Player p) { return !p.equals(runner); }
    public static Player getRunner() { return runner; }

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
        TrackedLocation trackedLocation = getRunnerLocation(p.getWorld());

        if (compass != null) {
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
        ItemStack compass = p.getInventory().getItem(p.getInventory().first(Material.COMPASS));
        World.Environment environment = Objects.requireNonNull(location.getWorld()).getEnvironment();

        if (environment == World.Environment.NORMAL) {
            p.setCompassTarget(runner.getLocation());
        } else if (environment == World.Environment.NETHER) {
            if (compass == null) return;
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            assert meta != null;
            meta.setItemName("Compass");
            meta.setLodestone(runner.getLocation());
            meta.setLodestoneTracked(false);
            compass.setItemMeta(meta);
        }
    }

    public static TrackedLocation getRunnerLocation(World world) {
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

    @EventHandler
    public void updateLastKnownLocation(PlayerPortalEvent event) {
        Player p = event.getPlayer();
        if (isRunner(p)) {
            lastKnownLocation.put(event.getFrom().getWorld(), event.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            List<String> lore = Objects.requireNonNull(drop.getItemMeta()).getLore();

            if (lore == null || lore.isEmpty()) continue;

            String marker = REMOVE_ON_DEATH_LORE.getFirst();
            String first = lore.getFirst();

            if (marker.equals(first)) {
                toRemove.add(drop);
            }
        }
        event.getDrops().removeAll(toRemove);
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
