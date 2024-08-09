package me.tsvrn9.minecraftmanhunt.features;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.tsvrn9.minecraftmanhunt.BaseTest;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrivateChatTest extends BaseTest {
    final static String MESSAGE = "I love taking long walks on the beach!";

    PlayerMock hunter2;
    Eavesdropper eve;

    @BeforeEach
    void setup() {
        loadFeatures(new PrivateChat());
        eve = new Eavesdropper();
        Bukkit.getPluginManager().registerEvents(eve, manhunt);
        hunter2 = server.addPlayer();
    }

    @Test
    void runnerCannotHearHunters() {
        hunter.chat(MESSAGE);
        server.getScheduler().waitAsyncEventsFinished();
        Collection<Audience> listeners = eve.events.getFirst().viewers();

        assertFalse(listeners.contains(runner));
    }

    @Test
    void huntersCanHearEachOther() {
        hunter.chat(MESSAGE);
        server.getScheduler().waitAsyncEventsFinished();
        Collection<Audience> listeners = eve.events.getFirst().viewers();

        assertTrue(listeners.contains(hunter2));
    }

    public static class Eavesdropper implements Listener {
        List<AsyncChatEvent> events = new ArrayList<>();

        @EventHandler
        void eavesdrop(AsyncChatEvent event) {
            events.add(event);
        }
    }
}
