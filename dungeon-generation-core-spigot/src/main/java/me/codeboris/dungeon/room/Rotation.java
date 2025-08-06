package me.codeboris.dungeon.room;

import me.codeboris.dungeon.util.Pos2d;
import org.bukkit.Location;

/**
 * Rotation system for dungeon rooms
 * NW = Northwest (default), NE = Northeast, SE = Southeast, SW = Southwest
 */
public enum Rotation {
    NW("northwest") {
        @Override
        public Location toRelative(Pos2d mapPoint, Location location) {
            return location.subtract(mapPoint.getMapX(), 0, mapPoint.getMapZ());
        }
        
        @Override
        public Location toActual(Pos2d mapPoint, Location location) {
            return location.add(mapPoint.getMapX(), 0, mapPoint.getMapZ());
        }
    },
    
    NE("northeast") {
        @Override
        public Location toRelative(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(), 
                z - mapPoint.getMapZ(), 
                location.getY(), 
                -x + mapPoint.getMapX());
        }
        
        @Override
        public Location toActual(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(),
                -z + mapPoint.getMapX(),
                location.getY(),
                x + mapPoint.getMapZ());
        }
    },
    
    SW("southwest") {
        @Override
        public Location toRelative(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(),
                -z + mapPoint.getMapZ(),
                location.getY(),
                x - mapPoint.getMapX());
        }
        
        @Override
        public Location toActual(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(),
                z + mapPoint.getMapX(),
                location.getY(),
                -x + mapPoint.getMapZ());
        }
    },
    
    SE("southeast") {
        @Override
        public Location toRelative(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(),
                -x + mapPoint.getMapX(),
                location.getY(),
                -z + mapPoint.getMapZ());
        }
        
        @Override
        public Location toActual(Pos2d mapPoint, Location location) {
            double x = location.getX();
            double z = location.getZ();
            return new Location(location.getWorld(),
                -x + mapPoint.getMapX(),
                location.getY(),
                -z + mapPoint.getMapZ());
        }
    };
    
    private final String name;
    
    Rotation(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Convert actual world coordinates to relative room coordinates
     */
    public abstract Location toRelative(Pos2d mapPoint, Location location);
    
    /**
     * Convert relative room coordinates to actual world coordinates
     */
    public abstract Location toActual(Pos2d mapPoint, Location location);
    
    /**
     * Get rotation from name string
     */
    public static Rotation fromName(String name) {
        for (Rotation rotation : values()) {
            if (rotation.name.equals(name)) {
                return rotation;
            }
        }
        throw new IllegalArgumentException(name + " is not a valid rotation");
    }
    
    /**
     * Get rotation from directional offset
     */
    public static Rotation fromOffset(int x, int z) {
        if (x == 0 && z == 1) return SE;
        if (x == 0 && z == -1) return NW;
        if (x == -1 && z == 0) return SW;
        if (x == 1 && z == 0) return NE;
        throw new IllegalArgumentException("Not a valid offset: x=" + x + ", z=" + z);
    }
}