package me.codeboris.dungeon.room;

import me.codeboris.dungeon.util.Pos2d;

import java.util.List;

/**
 * Represents a single room in the dungeon
 */
public class Room {
    private final RoomType type;
    private final RoomShape shape;
    private final Pos2d position;
    private final Rotation rotation;
    private final List<Pos2d> children;
    private final Pos2d parent;
    
    public Room(RoomType type, RoomShape shape, Pos2d position, Rotation rotation, List<Pos2d> children, Pos2d parent) {
        this.type = type;
        this.shape = shape;
        this.position = position;
        this.rotation = rotation;
        this.children = children;
        this.parent = parent;
    }
    
    public RoomType getType() {
        return type;
    }
    
    public RoomShape getShape() {
        return shape;
    }
    
    public Pos2d getPosition() {
        return position;
    }
    
    public Rotation getRotation() {
        return rotation;
    }
    
    public List<Pos2d> getChildren() {
        return children;
    }
    
    public Pos2d getParent() {
        return parent;
    }
    
    /**
     * Create a new room with different rotation
     */
    public Room withRotation(Rotation rotation) {
        return new Room(type, shape, position, rotation, children, parent);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return type == room.type &&
                shape == room.shape &&
                position.equals(room.position) &&
                rotation == room.rotation;
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + shape.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + rotation.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "Room{" +
                "type=" + type +
                ", shape=" + shape +
                ", position=" + position +
                ", rotation=" + rotation +
                '}';
    }
}