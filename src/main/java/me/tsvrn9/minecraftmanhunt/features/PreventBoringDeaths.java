package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.MinecraftManhunt;
import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PreventBoringDeaths implements Feature, Listener {
    @ConfigValue(value = "hunter.prevent_fire_tick_death")
    private boolean hunterPreventFireTickDeath = true;
    @ConfigValue(value = "hunter.prevent_fall_damage_death_in_end")
    private boolean hunterPreventFallDamageDeathInEnd = true;

    @ConfigValue(value = "runner.prevent_fire_tick_death")
    private boolean runnerPreventFireTickDeath = true;
    @ConfigValue(value = "runner.prevent_fall_damage_death_in_end")
    private boolean runnerPreventFallDamageDeathInEnd = true;

    @EventHandler
    public void modifyDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) event.getEntity();
            EntityDamageEvent.DamageCause cause = event.getCause();

            switch (cause) {
                case EntityDamageEvent.DamageCause.FIRE_TICK -> {
                    if (!((hunterPreventFireTickDeath && MinecraftManhunt.isHunter(p))
                            || (runnerPreventFireTickDeath && MinecraftManhunt.isRunner(p)))) return;

                    if (p.getHealth() - event.getFinalDamage() <= 0) {
                        p.setFireTicks(0);
                        event.setCancelled(true);
                    }
                }
                case EntityDamageEvent.DamageCause.FALL -> {
                    if (!((hunterPreventFallDamageDeathInEnd && MinecraftManhunt.isHunter(p))
                            || (runnerPreventFallDamageDeathInEnd && MinecraftManhunt.isRunner(p)))) return;

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
    @Override
    public String getPath() {
        return "prevent_boring_deaths";
    }
}
