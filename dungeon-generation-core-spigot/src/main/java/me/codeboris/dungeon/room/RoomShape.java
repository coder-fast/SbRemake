package me.codeboris.dungeon.room;

import me.codeboris.dungeon.util.Pos2d;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Different shapes/sizes that dungeon rooms can have
 */
public enum RoomShape {
    ONE_BY_ONE("1x1") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            return Collections.emptyList();
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(30, 0, 0);
                case SE:
                    return location.add(30, 0, 30);
                case SW:
                    return location.add(0, 0, 30);
                default:
                    return location;
            }
        }
    },
    
    ONE_BY_TWO("1x2") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Arrays.asList(target.add(0, 1));
                case NW:
                case SE:
                    return Arrays.asList(target.add(1, 0));
                default:
                    return Collections.emptyList();
            }
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(30, 0, 0);
                case SE:
                    return location.add(62, 0, 30);
                case SW:
                    return location.add(0, 0, 62);
                default:
                    return location;
            }
        }
    },
    
    ONE_BY_THREE("1x3") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Arrays.asList(target.add(0, 1), target.add(0, 2));
                case NW:
                case SE:
                    return Arrays.asList(target.add(1, 0), target.add(2, 0));
                default:
                    return Collections.emptyList();
            }
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(30, 0, 0);
                case SE:
                    return location.add(91, 0, 30);
                case SW:
                    return location.add(0, 0, 91);
                default:
                    return location;
            }
        }
    },
    
    ONE_BY_FOUR("1x4") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Arrays.asList(target.add(0, 1), target.add(0, 2), target.add(0, 3));
                case NW:
                case SE:
                    return Arrays.asList(target.add(1, 0), target.add(2, 0), target.add(3, 0));
                default:
                    return Collections.emptyList();
            }
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(30, 0, 0);
                case SE:
                    return location.add(126, 0, 30);
                case SW:
                    return location.add(0, 0, 126);
                default:
                    return location;
            }
        }
    },
    
    L_SHAPE("L-shape") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return Arrays.asList(target.add(0, 1), target.add(1, 1));
                case NE:
                    return Arrays.asList(target.add(-1, 0), target.add(-1, 1));
                case SE:
                    return Arrays.asList(target.add(0, -1), target.add(-1, -1));
                case SW:
                    return Arrays.asList(target.add(1, 0), target.add(1, -1));
                default:
                    return Collections.emptyList();
            }
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(30, 0, 0);
                case SE:
                    return location.add(30, 0, 30);
                case SW:
                    return location.add(0, 0, 30);
                default:
                    return location;
            }
        }
    },
    
    TWO_BY_TWO("2x2") {
        @Override
        public List<Pos2d> getChildren(Pos2d target, Rotation rotation) {
            return Arrays.asList(target.add(1, 0), target.add(1, 1), target.add(0, 1));
        }
        
        @Override
        public Location withRotationOffset(Location location, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return location;
                case NE:
                    return location.add(62, 0, 0);
                case SE:
                    return location.add(62, 0, 62);
                case SW:
                    return location.add(0, 0, 62);
                default:
                    return location;
            }
        }
    };
    
    private final String shape;
    
    RoomShape(String shape) {
        this.shape = shape;
    }
    
    @Override
    public String toString() {
        return shape;
    }
    
    /**
     * Get the child positions for multi-block rooms
     */
    public abstract List<Pos2d> getChildren(Pos2d target, Rotation rotation);
    
    /**
     * Apply rotation offset to location
     */
    public abstract Location withRotationOffset(Location location, Rotation rotation);
    
    /**
     * Try to insert this room shape at the given position
     * Returns true if successful, false if blocked
     */
    public boolean tryInsert(Room[][] rooms, Pos2d target, Rotation rotation) {
        if (rooms[target.getX()][target.getZ()] != null) {
            return false;
        }
        
        List<Pos2d> children = getChildren(target, rotation);
        for (Pos2d child : children) {
            if (child.getX() < 0 || child.getZ() < 0) return false;
            if (child.getX() >= rooms.length || child.getZ() >= rooms[0].length) return false;
            if (rooms[child.getX()][child.getZ()] != null) return false;
        }
        
        // Create the main room
        Room roomBase = new Room(RoomType.ROOM, this, target, rotation, children, null);
        rooms[target.getX()][target.getZ()] = roomBase;
        
        // Create child rooms pointing to parent
        for (Pos2d child : children) {
            rooms[child.getX()][child.getZ()] = new Room(RoomType.ROOM, this, child, rotation, Collections.<Pos2d>emptyList(), target);
        }
        
        return true;
    }
    
    /**
     * Convert relative position to actual world coordinates
     */
    public Location toActual(Pos2d target, Location blockPos, Rotation rotation) {
        Location pos = rotation.toActual(target, blockPos);
        return withRotationOffset(pos, rotation);
    }
    
    /**
     * Convert actual world coordinates to relative position
     */
    public Location toRelative(Pos2d target, Location actualPos, Rotation rotation) {
        Location rotOffset = withRotationOffset(new Location(actualPos.getWorld(), 0, 0, 0), rotation);
        Location pos = actualPos.subtract(rotOffset);
        return rotation.toRelative(target, pos);
    }
}