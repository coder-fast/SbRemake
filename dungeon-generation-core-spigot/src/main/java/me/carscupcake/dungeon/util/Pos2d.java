package me.carscupcake.dungeon.util;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * 2D position utility for dungeon grid coordinates
 * Each grid position represents a 32x32 block area
 */
public class Pos2d {
    private final int x;
    private final int z;
    
    public Pos2d(int x, int z) {
        this.x = x;
        this.z = z;
    }
    
    public int getX() {
        return x;
    }
    
    public int getZ() {
        return z;
    }
    
    /**
     * Convert to Bukkit Location
     */
    public Location toLocation(World world) {
        return new Location(world, x * 32, 70, z * 32);
    }
    
    /**
     * Get the actual world X coordinate (grid * 32)
     */
    public int getMapX() {
        return x * 32;
    }
    
    /**
     * Get the actual world Z coordinate (grid * 32)
     */
    public int getMapZ() {
        return z * 32;
    }
    
    /**
     * Add offset to this position
     */
    public Pos2d add(int x, int z) {
        return new Pos2d(this.x + x, this.z + z);
    }
    
    /**
     * Subtract another position from this one
     */
    public Pos2d sub(Pos2d other) {
        return new Pos2d(this.x - other.x, this.z - other.z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pos2d pos2d = (Pos2d) obj;
        return x == pos2d.x && z == pos2d.z;
    }
    
    @Override
    public int hashCode() {
        return 31 * x + z;
    }
    
    @Override
    public String toString() {
        return "Pos2d{x=" + x + ", z=" + z + "}";
    }
}