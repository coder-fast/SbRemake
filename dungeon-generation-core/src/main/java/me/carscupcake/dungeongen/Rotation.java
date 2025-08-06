package me.carscupcake.dungeongen;

public enum Rotation {
    NW("northwest"),
    NE("northeast"),
    SW("southwest"),
    SE("southeast");

    private final String name;

    Rotation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Rotation fromName(String name) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.name.equals(name)) {
                return rotation;
            }
        }
        throw new IllegalArgumentException(name + " is not a valid rotation");
    }

    public static Rotation fromOffset(int x, int z) {
        if (x == 0 && z == 1) return Rotation.SE;
        if (x == 0 && z == -1) return Rotation.NW;
        if (x == -1 && z == 0) return Rotation.SW;
        if (x == 1 && z == 0) return Rotation.NE;
        throw new IllegalArgumentException("Not a valid offset: x=" + x + ", z=" + z);
    }
}