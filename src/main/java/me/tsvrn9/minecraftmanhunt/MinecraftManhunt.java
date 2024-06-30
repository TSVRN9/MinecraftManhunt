package me.tsvrn9.minecraftmanhunt;

import me.tsvrn9.minecraftmanhunt.features.Features;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.generator.structure.Structure;
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
    private static ItemStack compass;
    private static List<ItemStack> opArmor;
    private static ItemStack opAxe;
    private static final List<String> OP_LORE = List.of(STR."\{ChatColor.COLOR_CHAR}O");

    private static final Map<World, Location> lastKnownLocation = new HashMap<>();

    private static Player runner = null;
    private static boolean opgear = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Features.registerConfigurationSerializables();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        assert compassMeta != null;
        compassMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        compassMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        compass.setItemMeta(compassMeta);

        List<ItemStack> opArmor = Stream.of(
                        Material.DIAMOND_BOOTS,
                        Material.DIAMOND_LEGGINGS,
                        Material.DIAMOND_CHESTPLATE,
                        Material.DIAMOND_HELMET
                )
                .map(ItemStack::new)
                .peek(i -> {
                    i.addEnchantment(Enchantment.PROTECTION, 4);
                    i.addEnchantment(Enchantment.UNBREAKING, 3);
                    ItemMeta meta = i.getItemMeta();
                    assert meta != null;
                    meta.setLore(OP_LORE);
                    i.setItemMeta(meta);
                })
                .toList();

        ItemStack opAxe = new ItemStack(Material.DIAMOND_AXE);
        opAxe.addEnchantment(Enchantment.SHARPNESS, 1);
        opAxe.addEnchantment(Enchantment.UNBREAKING, 3);

        MinecraftManhunt.compass = compass;
        MinecraftManhunt.opArmor = opArmor;
        MinecraftManhunt.opAxe = opAxe;

    }

    @Override
    public void onDisable() {
        Features.save(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return switch (args.length) {
            case 1 -> Stream.of("speedrunner", "opgear", "runnerbuffs", "hunterbuffs", "giveitems", "reset", "private")
                    .filter(s -> s.startsWith(args[0])).toList();
            case 2 -> {
                if (args[0].equalsIgnoreCase("speedrunner")) {
                    yield null;
                } else {
                    yield List.of();
                }
            }
            default -> List.of();
        };
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
                    Player player = Bukkit.getPlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage(STR."\{ChatColor.RED}Player not found!");
                        return false;
                    }

                    if (runner == null) {
                        // first chosen, so we set-up all of our features here
                        Features.load(this);
                    }

                    runner = player;
                    sender.sendMessage(STR."\{ChatColor.GREEN}\{player.getName()} is the speedrunner!");
                }
                case "setting" -> {

                }
                case "save" -> {
                    saveConfig();
                }
                case "reload" -> {
                    reloadConfig();
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
        world.setTime(1000);
    }

    public static boolean isRunner(Player p) { return p.equals(runner); }
    public static boolean isHunter(Player p) { return !p.equals(runner); }
    public static Player getRunner() { return runner; }

    public static void giveHunterGear(Player p) {
        if (opgear) {
            ItemStack[] armor = opArmor.toArray(new ItemStack[0]);
            p.getInventory().setArmorContents(armor);
            p.getInventory().addItem(opAxe);
        }
        p.getInventory().addItem(compass);
    }

    public static boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    public static void giveItems() {
        Bukkit.getOnlinePlayers().stream()
                .filter(MinecraftManhunt::isHunter)
                .forEach(MinecraftManhunt::giveHunterGear);
    }

    public static String updateCompass(Player p) {
        ItemStack compass = p.getInventory().getItem(p.getInventory().first(Material.COMPASS));

        if (runner == null) return STR."\{ChatColor.RED}Use \"/mm speedrunner <name>\" to set a speedrunner";

        if (compass != null)
            if (isHunter(p)) {
                Location loc;
                boolean isSameWorld = runner.getWorld().equals(p.getWorld());
                World.Environment environment = runner.getWorld().getEnvironment();

                if (isSameWorld) {
                    loc = runner.getLocation();
                } else {
                    loc = lastKnownLocation.get(p.getWorld());
                }

                if (loc == null) {
                    return STR."\{ChatColor.RED}No Location Found...";
                }

                if (environment == World.Environment.NORMAL) {
                    p.setCompassTarget(runner.getLocation());
                } else {
                    CompassMeta meta = (CompassMeta) compass.getItemMeta();
                    assert meta != null;
                    meta.setItemName("Compass");
                    meta.setLodestone(runner.getLocation());
                    meta.setLodestoneTracked(false);
                    compass.setItemMeta(meta);
                }

                if (isSameWorld) {
                    return STR."\{ChatColor.YELLOW}Updated Compass";
                } else {
                    return STR."\{ChatColor.YELLOW}Updated Compass to Last Known Location";
                }
            } else {
                if (runnerbuffs && p.getWorld().getEnvironment() == World.Environment.NETHER) {
                    World nether = p.getWorld();

                    Location result = Objects.requireNonNull(nether.locateNearestStructure(runner.getLocation(), Structure.FORTRESS, 750, false)).getLocation();

                    CompassMeta meta = (CompassMeta) compass.getItemMeta();
                    assert meta != null;
                    meta.setItemName("Compass");
                    meta.setLodestone(result);
                    meta.setLodestoneTracked(false);
                    compass.setItemMeta(meta);

                    return STR."\{ChatColor.YELLOW}Updated Compass to Closest Nether Fortress";
                }
            }
        return STR."\{ChatColor.RED}How did you get here??";
    }

    public static TrackedLocation getLastKnownLocation(World world) {
        boolean isOutdated = !runner.getWorld().equals(world);
        Location location = isOutdated ? lastKnownLocation.get(world) : runner.getLocation();

        return new TrackedLocation(location, isOutdated);
    }

    @EventHandler
    public void rightClickCompass(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (runner != null && item != null && isRightClick(event.getAction()) && item.getType() == Material.COMPASS) {
            Player p = event.getPlayer();

            p.sendMessage(updateCompass(p));
        }
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
        event.getDrops().remove(compass);
        if (opgear) {
            List<ItemStack> toRemove = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (OP_LORE.equals(Objects.requireNonNull(drop.getItemMeta()).getLore())) {
                    toRemove.add(drop);
                }
            }
            event.getDrops().removeAll(toRemove);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isHunter(player)) {
            giveHunterGear(player);
        }
    }
}
