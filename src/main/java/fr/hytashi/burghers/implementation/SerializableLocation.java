package fr.hytashi.burghers.implementation;

import java.io.*;
import org.bukkit.*;

public class SerializableLocation implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public SerializableLocation(final Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.worldName = loc.getWorld().getName();
    }

    public Location toBukkitLocation() {
        return new Location(this.getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    public String getWorldName() {
        return this.worldName;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
