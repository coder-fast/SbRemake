package me.carscupcake.dungeongen;

import java.util.List;

public class Room {
    private final RoomType type;
    private final RoomShape shape;
    private final Pos2d pos;
    private final Rotation rotation;
    private final List<Pos2d> children;
    private final Pos2d parent;

    public Room(RoomType type, RoomShape shape, Pos2d pos, Rotation rotation, List<Pos2d> children, Pos2d parent) {
        this.type = type;
        this.shape = shape;
        this.pos = pos;
        this.rotation = rotation;
        this.children = children;
        this.parent = parent;
    }

    public RoomType getType() { return type; }
    public RoomShape getShape() { return shape; }
    public Pos2d getPos() { return pos; }
    public Rotation getRotation() { return rotation; }
    public List<Pos2d> getChildren() { return children; }
    public Pos2d getParent() { return parent; }

    public Room withRotation(Rotation rotation) {
        return new Room(type, shape, pos, rotation, children, parent);
    }
}