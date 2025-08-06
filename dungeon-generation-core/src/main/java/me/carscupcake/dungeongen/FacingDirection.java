package me.carscupcake.dungeongen;

public enum FacingDirection {
    North("north"),
    East("east"),
    South("south"),
    West("west"),
    Up("up"),
    Down("down");

    private final String id;

    FacingDirection(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static FacingDirection fromId(String id) {
        for (FacingDirection direction : FacingDirection.values()) {
            if (direction.id.equals(id)) {
                return direction;
            }
        }
        return null;
    }

    public FacingDirection next() {
        switch (this) {
            case North: return East;
            case East: return South;
            case South: return West;
            case West: return North;
            default: return this;
        }
    }
}