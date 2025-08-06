package me.carscupcake.dungeongen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RoomShape {
    ONE_BY_ONE {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            return Collections.emptyList();
        }
    },
    ONE_BY_TWO {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Collections.singletonList(target.add(0, 1));
                case NW:
                case SE:
                    return Collections.singletonList(target.add(1, 0));
            }
            return Collections.emptyList();
        }
    },
    ONE_BY_THREE {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Arrays.asList(target.add(0, 1), target.add(0, 2));
                case NW:
                case SE:
                    return Arrays.asList(target.add(1, 0), target.add(2, 0));
            }
            return Collections.emptyList();
        }
    },
    ONE_BY_FOUR {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NE:
                case SW:
                    return Arrays.asList(target.add(0, 1), target.add(0, 2), target.add(0, 3));
                case NW:
                case SE:
                    return Arrays.asList(target.add(1, 0), target.add(2, 0), target.add(3, 0));
            }
            return Collections.emptyList();
        }
    },
    L_SHAPE {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            switch (rotation) {
                case NW:
                    return Arrays.asList(target.add(0, 1), target.add(1, 1));
                case NE:
                    return Arrays.asList(target.add(-1, 0), target.add(-1, 1));
                case SE:
                    return Arrays.asList(target.add(0, -1), target.add(-1, -1));
                case SW:
                    return Arrays.asList(target.add(1, 0), target.add(1, -1));
            }
            return Collections.emptyList();
        }
    },
    TWO_BY_TWO {
        @Override
        public List<Pos2d> children(Pos2d target, Rotation rotation) {
            return Arrays.asList(target.add(1, 0), target.add(1, 1), target.add(0, 1));
        }
    };

    public abstract List<Pos2d> children(Pos2d target, Rotation rotation);

    public boolean tryInsert(Room[][] rooms, Pos2d target, Rotation rotation) {
        if (rooms[target.x()][target.z()] != null) return false;
        List<Pos2d> children = children(target, rotation);
        for (Pos2d child : children) {
            if (child.x() < 0 || child.z() < 0) return false;
            if (child.x() >= rooms.length || child.z() >= rooms[0].length) return false;
            if (rooms[child.x()][child.z()] != null) return false;
        }
        Room roomBase = new Room(RoomType.Room, this, target, rotation, children, null);
        rooms[target.x()][target.z()] = roomBase;
        for (Pos2d child : children) {
            rooms[child.x()][child.z()] = new Room(RoomType.Room, this, child, rotation, Collections.emptyList(), target);
        }
        return true;
    }
}