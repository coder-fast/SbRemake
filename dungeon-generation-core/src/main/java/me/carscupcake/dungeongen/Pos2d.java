package me.carscupcake.dungeongen;

import java.util.Objects;

public class Pos2d {
    private final int x;
    private final int z;

    public Pos2d(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() { return x; }
    public int z() { return z; }

    public Pos2d add(int dx, int dz) {
        return new Pos2d(x + dx, z + dz);
    }

    public Pos2d sub(Pos2d other) {
        return new Pos2d(x - other.x, z - other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos2d pos2d = (Pos2d) o;
        return x == pos2d.x && z == pos2d.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "Pos2d{" + "x=" + x + ", z=" + z + '}';
    }
}