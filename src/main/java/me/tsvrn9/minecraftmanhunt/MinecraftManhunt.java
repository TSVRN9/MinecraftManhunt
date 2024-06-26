package me.tsvrn9.minecraftmanhunt;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private static final PotionEffect SPEED_III = new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2);
    private static final PotionEffect SPEED_II = new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1);
    private static final PotionEffect SPEED_I = new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0);

    private static Player runner = null;
    private static boolean opgear = false;
    private static boolean hunterbuffs = true;
    private static boolean runnerbuffs = true;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

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

        this.compass = compass;
        this.opArmor = opArmor;
        this.opAxe = opAxe;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!hunterbuffs || runner == null ) return; // minimal impact cuz this is the only plugin & this isn't prod

            for (Player hunter : Bukkit.getOnlinePlayers()) {
                Location location = runner.getWorld().equals(hunter.getWorld()) ? runner.getLocation() : lastKnownLocation.get(hunter.getWorld());

                if (location == null) return;

                double distanceSquared = location.distanceSquared(hunter.getLocation());
                PotionEffect speedBuff = calculateSpeedBuff(distanceSquared);

                if (speedBuff == null) {
                    hunter.removePotionEffect(PotionEffectType.SPEED);
                } else {
                    PotionEffect currentSpeed = hunter.getActivePotionEffects().stream()
                            .filter(p -> p.getType() == PotionEffectType.SPEED).findFirst().orElse(null);
                    if (currentSpeed != null && currentSpeed.getAmplifier() > speedBuff.getAmplifier()) {
                        hunter.removePotionEffect(PotionEffectType.SPEED);
                    }
                    hunter.addPotionEffect(speedBuff);
                }
            }
        }, 0, 20*10);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(this::updateCompass);
        }, 0, 20*4);
    }

    private PotionEffect calculateSpeedBuff(double distanceSquared) {
        if (distanceSquared > 2250*2250) {
            return SPEED_III;
        } else if (distanceSquared > 1500*1500) {
            return SPEED_II;
        } else if (distanceSquared > 750*750) {
            return SPEED_I;
        } else {
            return null;
        }
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

                    runner = player;
                    sender.sendMessage(STR."\{ChatColor.GREEN}\{player.getName()} is the speedrunner!");
                }
                case "opgear" -> {
                    opgear =  !opgear;
                    sender.sendMessage(STR."\{ChatColor.GREEN}OP Hunter gear is now \{ChatColor.BOLD}\{opgear ? "enabled" : "disabled"}");
                }
                case "runnerbuffs" -> {
                    runnerbuffs =  !runnerbuffs;
                    sender.sendMessage(STR."\{ChatColor.GREEN}Runner buffs are now \{ChatColor.BOLD}\{runnerbuffs ? "enabled" : "disabled"}");
                }
                case "hunterbuffs" -> {
                    hunterbuffs =  !hunterbuffs;
                    sender.sendMessage(STR."\{ChatColor.GREEN}Hunter buffs are now \{ChatColor.BOLD}\{hunterbuffs ? "enabled" : "disabled"}");
                }
                case "private" -> {
                    if (sender instanceof Player player) {
                        boolean usingPrivate = !usingPrivateChannel.get(player);
                        usingPrivateChannel.put(player, usingPrivate);
                        sender.sendMessage(STR."\{ChatColor.GREEN}Private channel is now \{ChatColor.BOLD}\{usingPrivate ? "enabled" : "disabled"}");
                    } else {
                        sender.sendMessage("Nice try console...");
                    }
                }
                case "giveitems" -> giveItems();
                case "reset" -> reset();
                default -> {
                    sender.sendMessage(STR."\{ChatColor.RED}Not a valid command!");
                    return false;
                }

            }
        } else { // label == "shout"
            if (args.length == 0 && sender instanceof Player player) {
                usingPrivateChannel.put(player, false);
                sender.sendMessage(STR."\{ChatColor.GREEN}Private channel is now \{ChatColor.BOLD}disabled");
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    builder.append(args[i]);
                    if (i != args.length - 1) {
                        builder.append(" ");
                    }
                }

                Bukkit.broadcastMessage(STR."<\{sender.getName()}> \{builder.toString()}");
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

    public void giveHunterGear(Player p) {
        if (opgear) {
            ItemStack[] armor = opArmor.toArray(new ItemStack[0]);
            p.getInventory().setArmorContents(armor);
            p.getInventory().addItem(opAxe);
        }
        p.getInventory().addItem(compass);
    }
    public boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }


    public void giveItems() {
        Bukkit.getOnlinePlayers().stream()
                .filter(this::isHunter)
                .forEach(this::giveHunterGear);
    }

    public String updateCompass(Player p) {
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
    public void modifyDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) event.getEntity();
            EntityDamageEvent.DamageCause cause = event.getCause();

            if (runnerbuffs && isRunner(p)) {
                switch (cause) {
                    case EntityDamageEvent.DamageCause.FIRE_TICK -> {
                        if (p.getHealth() - event.getFinalDamage() <= 0) {
                            p.setFireTicks(0);
                            event.setCancelled(true);
                        }
                    }
                    case EntityDamageEvent.DamageCause.FALL -> {
                        if (p.getWorld().getEnvironment() == World.Environment.THE_END) {
                            damageToHalfAHeart(event);
                        }
                    }
                }
            }

            if (hunterbuffs && isHunter(p)) {
                if (cause == EntityDamageEvent.DamageCause.FALL) {
                    if (p.getWorld().getEnvironment() == World.Environment.THE_END) {
                        damageToHalfAHeart(event);
                    }
                }
            }
        }
    }

    public void damageToHalfAHeart(EntityDamageEvent event) {
        LivingEntity e = (LivingEntity) event.getEntity();
        if (e.getHealth() > .5) {
            e.setHealth(
                    Math.max(e.getHealth() - event.getFinalDamage(), 0.5)
            );
            event.setDamage(0);
        }

    }

    @EventHandler
    public void buffTrades(PiglinBarterEvent event) {
        if (runnerbuffs && Math.random() < .2) {
            event.getOutcome().add(new ItemStack(Material.ENDER_PEARL, (int) Math.ceil(Math.random() * 2)));
        }
    }

    @EventHandler
    public void buffBlazes(EntityDeathEvent event) {
        if (runnerbuffs && event.getEntityType() == EntityType.BLAZE) {
            event.getDrops().clear();
            if (Math.random() < .75)
                event.getDrops().add(new ItemStack(Material.BLAZE_ROD));
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
