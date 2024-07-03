package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Timer implements Feature, CommandExecutor, TabCompleter {
    @ConfigValue("default_timer_duration_in_seconds")
    private long defaultTimerDurationInSeconds = 60L;

    private BukkitRunnable runnable;

    @Override
    public void onDisable(Plugin plugin) {
        stopTimer();
    }

    @Override
    public String[] getHandledCommands() {
        return new String[] {"timer"};
    }

    @Override
    public String getPath() {
        return "timer";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,  String[] args) {
        // TODO
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

    private void stopTimer() {
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }
    }

    private static class TimerTask extends BukkitRunnable {
        private long durationInSeconds;
        private TimerTask(long durationInSeconds) {
            this.durationInSeconds = durationInSeconds;
        }

        @Override
        public void run() {
            // TODO
        }
    }
}
