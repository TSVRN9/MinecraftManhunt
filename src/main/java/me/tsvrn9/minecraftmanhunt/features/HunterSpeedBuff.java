package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.TrackedLocation;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HunterSpeedBuff implements Feature, Listener {
    @ConfigValue("thresholds")
    private List<SpeedThreshold> thresholds = getDefaultThresholds();

    @ConfigValue("check_frequency_in_ticks")
    private long checkInTicks = 60L;

    private final BukkitRunnable bukkitRunnable = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player hunter : Bukkit.getOnlinePlayers()) {
                TrackedLocation trackedLocation = MinecraftManhunt.getRunnerLocation(hunter.getWorld());

                if (!trackedLocation.exists()) return;

                double distanceSquared = trackedLocation.location().distanceSquared(hunter.getLocation());
                PotionEffect speedBuff = getSpeedBuff(distanceSquared);

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
        }
    };

    private PotionEffect getSpeedBuff(double distanceSquared) {
        for (SpeedThreshold threshold : thresholds) {
            if (threshold.distanceThreshold*threshold.distanceThreshold > distanceSquared) {
                return new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, threshold.amplifier);
            }
        }
        return null; // remove effect
    }

    @Override
    public void onEnable(Plugin plugin) {
        thresholds.sort(
                Comparator.comparingDouble(a -> a.distanceThreshold)
        );

        bukkitRunnable.runTaskTimer(plugin, 0L, checkInTicks);
    }

    @Override
    public void onDisable(Plugin plugin) {
        if (!bukkitRunnable.isCancelled())
            bukkitRunnable.cancel();
    }

    @Override
    public String getPath() {
        return "hunter_speed_buff";
    }

    @Override
    public Class<?>[] getConfigurationSerializables() {
        return new Class<?>[] { SpeedThreshold.class };
    }


    private List<SpeedThreshold> getDefaultThresholds() {
        return new ArrayList<>(List.of(
            new SpeedThreshold(2, 2250),
            new SpeedThreshold(1, 1500),
            new SpeedThreshold(0, 750)
        ));
    }

    private record SpeedThreshold(int amplifier, double distanceThreshold) implements ConfigurationSerializable {
        public SpeedThreshold {
            if (amplifier < 0) throw new IllegalArgumentException("Amplifier cannot be negative");
            if (distanceThreshold < 0) throw new IllegalArgumentException("Amplifier cannot be negative");
        }

        @SuppressWarnings("unused")
        public static SpeedThreshold deserialize(Map<String, Object> map) {
            int amplifier = Integer.parseInt((String) map.get("amplifier"));
            double radius = Double.parseDouble((String) map.get("distance_threshold"));

            return new SpeedThreshold(amplifier, radius);
        }

        @Override
        public Map<String, Object> serialize() {
            return Map.of(
                    "amplifier", Integer.toString(amplifier),
                    "distance_threshold", Double.toString(distanceThreshold)
            );
        }
    }
}
