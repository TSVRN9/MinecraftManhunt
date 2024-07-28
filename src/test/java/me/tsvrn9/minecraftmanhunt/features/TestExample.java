package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestExample {
    @Test
    void test() {
        ServerMock server = MockBukkit.mock();
        PlayerMock player = server.addPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0));

        server.getScheduler().performTicks(10L);

        assertFalse(player.getActivePotionEffects().isEmpty());
    }
}
