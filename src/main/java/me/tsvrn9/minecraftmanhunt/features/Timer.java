package me.tsvrn9.minecraftmanhunt.features;

import me.tsvrn9.minecraftmanhunt.configuration.ConfigValue;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Timer implements Feature, CommandExecutor, TabCompleter {
    @ConfigValue("default_timer_duration_in_seconds")
    private int defaultTimerDurationInSeconds = 60;

    protected TimerTask timer;
    private Plugin plugin;

    @Override
    public void onEnable(Plugin plugin) {
        this.plugin = plugin;
    }

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
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,  String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            sender.sendMessage(STR."\{ChatColor.RED}Timer stopped!");
            stopTimer();
            return true;
        }

        int seconds;

        if (args.length == 0) {
            seconds = defaultTimerDurationInSeconds;
        } else {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        stopTimer(); // stop any existing timer
        timer = new TimerTask(seconds);
        timer.startTimer(plugin);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 && "stop".startsWith(args[0]) ? List.of("stop") : List.of();
    }

    private void stopTimer() {
        if (timer != null && !timer.isCancelled()) {
            timer.cancel();
        }
    }

    protected static class TimerTask extends BukkitRunnable {
        private int durationInSeconds;
        private TimerTask(int durationInSeconds) {
            this.durationInSeconds = durationInSeconds;
        }

        @Override
        public void run() {
            if (durationInSeconds == 0) {
                Bukkit.broadcastMessage(STR."\{ChatColor.GREEN}Go!");
                cancel();
            }

            boolean overMinute = durationInSeconds > 60;
            boolean showMessage = !overMinute ? switch (durationInSeconds) {
                case 60, 30, 10, 5, 4, 3, 2, 1 -> true;
                default -> false;
            } : durationInSeconds % 60 == 0;

            if (showMessage) {
                if (overMinute) {
                    Bukkit.broadcastMessage(STR."\{ChatColor.GREEN}\{durationInSeconds/60} minutes left!");
                } else {
                    Bukkit.broadcastMessage(STR."\{ChatColor.GREEN}\{durationInSeconds} second\{durationInSeconds != 1 ? "s" : ""}!");
                }
            }

            durationInSeconds--;
        }

        public void startTimer(Plugin plugin) {
            this.runTaskTimerAsynchronously(plugin, 0L, 20L);
        }
    }
}
