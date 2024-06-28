package me.tsvrn9.minecraftmanhunt;

import org.bukkit.Location;

public record TrackedLocation(Location location, boolean isOutdated) {
    public boolean exists() { return location != null; }
}
