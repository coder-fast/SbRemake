package me.carscupcake.dungeongen;

import java.util.*;

public class Generator {
    private static final int[][] DIRECTIONS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };
    private final Random random;
    private final DoorType[][] doorsVertical;
    private final DoorType[][] doorsHorizontal;
    private final Room entrance;
    private final Room blood;
    private final Room fairy;
    private final Room trap;
    private Room[][] rooms;

    public Generator(Room[][] dimensions) {
        this(dimensions, System.currentTimeMillis());
    }

    public Generator(Room[][] dimensions, long seed) {
        this.rooms = dimensions;
        this.doorsVertical = new DoorType[rooms.length][rooms[0].length - 1];
        this.doorsHorizontal = new DoorType[rooms.length - 1][rooms[0].length];
        this.random = new Random(seed);
        Integer[] xEs = new Integer[dimensions.length];
        int zLength = dimensions[0].length;
        for (int i = 0; i < dimensions.length; i++) {
            xEs[i] = i;
            if (dimensions[i].length != zLength) throw new IllegalArgumentException("Dimensions lengths don't match");
        }
        Integer[] zEs = new Integer[zLength];
        for (int i = 0; i < zLength; i++) {
            zEs[i] = i;
        }
        shuffleArray(xEs);
        shuffleArray(zEs);

        Pos2d entrancePos = new Pos2d(0, random.nextInt(rooms[0].length));
        entrance = new Room(RoomType.Entrance, RoomShape.ONE_BY_ONE, entrancePos, Rotation.NW, Collections.emptyList(), null);
        rooms[entrancePos.x()][entrancePos.z()] = entrance;

        int redZ = random.nextInt(rooms[0].length);
        int redX = rooms.length - 1;
        if (redZ == 0 || redZ == rooms[0].length - 1)
            if (random.nextBoolean())
                redX--;
        blood = new Room(RoomType.Blood, RoomShape.ONE_BY_ONE, new Pos2d(redX, redZ), Rotation.NW, Collections.emptyList(), null);
        rooms[redX][redZ] = blood;

        List<Pos2d> fairyPossibilities = new ArrayList<Pos2d>();
        for (int x = 1; x < rooms.length - 1; x++) {
            for (int z = 1; z < rooms[x].length - 1; z++) {
                if (testForFairy(x, z))
                    fairyPossibilities.add(new Pos2d(x, z));
            }
        }
        Pos2d fairyPos = fairyPossibilities.get(random.nextInt(fairyPossibilities.size()));
        fairy = new Room(RoomType.Fairy, RoomShape.ONE_BY_ONE, fairyPos, Rotation.NW, Collections.emptyList(), null);
        rooms[fairyPos.x()][fairyPos.z()] = fairy;

        List<Pos2d> trapSpots = new ArrayList<Pos2d>(6);
        trapSpots.add(fairyPos.add(1, 0));
        trapSpots.add(fairyPos.add(0, 1));
        trapSpots.add(fairyPos.add(-1, 0));
        trapSpots.add(fairyPos.add(0, -1));
        if (entrancePos.z() != 0) trapSpots.add(entrancePos.add(0, -1));
        if (entrancePos.z() != rooms[0].length - 1) trapSpots.add(entrancePos.add(0, 1));
        Pos2d trapPos = trapSpots.get(random.nextInt(trapSpots.size()));
        trap = new Room(RoomType.Trap, RoomShape.ONE_BY_ONE, trapPos, Rotation.NW, Collections.emptyList(), null);
        rooms[trapPos.x()][trapPos.z()] = trap;

        int puzzlesAmount = 2 + random.nextInt(3);
        for (int i = 0; i < puzzlesAmount; i++) {
            int x;
            int z;
            do {
                x = random.nextInt(rooms.length);
                z = random.nextInt(rooms[0].length);
            } while (!canPlacePuzzle(x, z));
            rooms[x][z] = new Room(RoomType.Puzzle, RoomShape.ONE_BY_ONE, new Pos2d(x, z), Rotation.NW, Collections.emptyList(), null);
        }

        for (int x : xEs)
            for (int z : zEs) {
                tryPlace(new Pos2d(x, z));
            }
        for (int x : xEs)
            for (int z : zEs) {
                if (rooms[x][z] == null)
                    RoomShape.ONE_BY_ONE.tryInsert(rooms, new Pos2d(x, z), Rotation.values()[random.nextInt(4)]);
            }
    }

    private boolean canPlacePuzzle(int x, int z) {
        if (rooms[x][z] != null) return false;
        return (x + 1 == rooms.length || rooms[x + 1][z] == null) && (x == 0 || rooms[x - 1][z] == null);
    }

    private void tryPlace(Pos2d pos2d) {
        RoomShape[] shapes = new RoomShape[]{RoomShape.ONE_BY_TWO, RoomShape.ONE_BY_THREE, RoomShape.ONE_BY_FOUR, RoomShape.L_SHAPE, RoomShape.TWO_BY_TWO};
        shuffleArray(shapes);
        for (RoomShape shape : shapes) {
            Rotation rot = Rotation.values()[random.nextInt(4)];
            if (shape.tryInsert(rooms, pos2d, rot)) {
                if (shape == RoomShape.TWO_BY_TWO) {
                    doorsHorizontal[pos2d.x()][pos2d.z()] = DoorType.Bridge;
                    doorsVertical[pos2d.x()][pos2d.z()] = DoorType.Bridge;
                    doorsHorizontal[pos2d.x()][pos2d.z() + 1] = DoorType.Bridge;
                    doorsVertical[pos2d.x() + 1][pos2d.z()] = DoorType.Bridge;
                } else {
                    Pos2d prev = null;
                    for (Pos2d children : shape.children(pos2d, rot)) {
                        if (shape == RoomShape.ONE_BY_TWO || shape == RoomShape.ONE_BY_THREE || shape == RoomShape.ONE_BY_FOUR) {
                            if (rot == Rotation.NE || rot == Rotation.SW) {
                                doorsVertical[children.x()][children.z() - 1] = DoorType.Bridge;
                            } else if (rot == Rotation.SE || rot == Rotation.NW) {
                                doorsHorizontal[children.x() - 1][children.z()] = DoorType.Bridge;
                            }
                        } else if (shape == RoomShape.L_SHAPE) {
                            Pos2d off = prev == null ? pos2d : prev;
                            Pos2d abs = children.sub(off);
                            if (abs.x() != 0) {
                                doorsHorizontal[off.x() + (abs.x() > 0 ? 0 : -1)][off.z()] = DoorType.Bridge;
                            } else {
                                doorsVertical[off.x()][off.z() + (abs.z() > 0 ? 0 : -1)] = DoorType.Bridge;
                            }
                            prev = children;
                        }
                    }
                }
                return;
            }
        }
    }

    private boolean testForFairy(int x, int z) {
        return rooms[x + 1][z] == null && rooms[x][z + 1] == null &&
                rooms[x - 1][z] == null && rooms[x][z - 1] == null;
    }

    private <T> void shuffleArray(T[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public Room getFromPos(Pos2d pos2d) {
        return rooms[pos2d.x()][pos2d.z()];
    }

    public void generateDoors(Pos2d start) {
        boolean[][] discovered = new boolean[rooms.length][rooms[0].length];
        LinkedList<Pos2d> posebilities = new LinkedList<Pos2d>();
        posebilities.add(start);
        while (!posebilities.isEmpty()) {
            Room current = getFromPos(posebilities.pop());
            if (current.getType() != RoomType.Room && current.getType() != RoomType.Fairy) {
                if (current.getType() == RoomType.Entrance) doorsHorizontal[0][current.getPos().z()] = DoorType.Start;
                continue;
            }
            if (testPos(current.getPos(), 1, 0, discovered)) {
                addDoor(current.getPos().add(1, 0), posebilities, discovered);
                doorsHorizontal[current.getPos().x()][current.getPos().z()] = DoorType.Normal;
            }
            if (testPos(current.getPos(), -1, 0, discovered)) {
                addDoor(current.getPos().add(-1, 0), posebilities, discovered);
                doorsHorizontal[current.getPos().x() - 1][current.getPos().z()] = getFromPos(current.getPos().add(-1, 0)).getType() == RoomType.Entrance ? DoorType.Start : DoorType.Normal;
            }
            if (testPos(current.getPos(), 0, 1, discovered)) {
                addDoor(current.getPos().add(0, 1), posebilities, discovered);
                doorsVertical[current.getPos().x()][current.getPos().z()] = DoorType.Normal;
            }
            if (testPos(current.getPos(), 0, -1, discovered)) {
                addDoor(current.getPos().add(0, -1), posebilities, discovered);
                doorsVertical[current.getPos().x()][current.getPos().z() - 1] = DoorType.Normal;
            }
        }
        dijkstraPath(entrance.getPos(), fairy.getPos());
        dijkstraPath(fairy.getPos(), blood.getPos());
        fixSpecialRoomRotations();
    }

    private void addDoor(Pos2d targetPos, List<Pos2d> posebilities, boolean[][] discovered) {
        discovered[targetPos.x()][targetPos.z()] = true;
        Room target = getFromPos(targetPos);
        if (target.getParent() != null) {
            target = getFromPos(target.getParent());
        }
        for (Pos2d poses : target.getChildren()) {
            if (!posebilities.contains(poses))
                posebilities.add(poses);
            discovered[poses.x()][poses.z()] = true;
        }
        if (!posebilities.contains(target.getPos()))
            posebilities.add(target.getPos());
    }

    private boolean testPos(Pos2d pos2d, int x, int z, boolean[][] discovered) {
        if (pos2d.x() + x < 0 || pos2d.z() + z < 0 || pos2d.x() + x >= rooms.length || pos2d.z() + z >= rooms[0].length)
            return false;
        Room door = getFromPos(pos2d.add(x, z));
        if (door.getType() == RoomType.Entrance) {
            if (x != -1) return false;
        }
        return !discovered[pos2d.x() + x][pos2d.z() + z];
    }

    public void dijkstraPath(Pos2d start, Pos2d end) {
        int[][] dists = new int[rooms.length][rooms[0].length];
        int numRows = rooms.length;
        int numCols = rooms[0].length;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                dists[i][j] = Integer.MAX_VALUE;
            }
        }
        Node[][] parent = new Node[numRows][numCols];
        PriorityQueue<Node> pq = new PriorityQueue<Node>(new Comparator<Node>() {
            public int compare(Node a, Node b) {
                return Integer.compare(a.dist, b.dist);
            }
        });
        dists[start.x()][start.z()] = 0;
        pq.add(new Node(start.x(), start.z(), 0));

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            int r = currentNode.row;
            int c = currentNode.col;
            int currentDist = currentNode.dist;
            if (currentDist > dists[r][c]) {
                continue;
            }
            if (r == end.x() && c == end.z()) {
                reconstructPath(parent, new Node(end.x(), end.z(), currentDist));
                return;
            }
            for (int[] dir : DIRECTIONS) {
                int newR = r + dir[0];
                int newC = c + dir[1];
                if (newR >= 0 && newR < numRows && newC >= 0 && newC < numCols) {
                    boolean canMove = false;
                    if (dir[0] == -1) {
                        if (r > 0) {
                            DoorType type = doorsHorizontal[r - 1][c];
                            canMove = type == DoorType.Normal || type == DoorType.Bridge;
                        }
                    } else if (dir[0] == 1) {
                        if (r < numRows - 1) {
                            DoorType type = doorsHorizontal[r][c];
                            canMove = type == DoorType.Normal || type == DoorType.Start || type == DoorType.Bridge;
                        }
                    } else if (dir[1] == -1) {
                        if (c > 0) {
                            DoorType type = doorsVertical[r][c - 1];
                            canMove = type == DoorType.Normal || type == DoorType.Bridge;
                        }
                    } else if (dir[1] == 1) {
                        if (c < numCols - 1) {
                            DoorType type = doorsVertical[r][c];
                            canMove = type == DoorType.Normal || type == DoorType.Bridge;
                        }
                    }
                    if (canMove) {
                        Room room = rooms[newR][newC];
                        int newDist = currentDist + 1 + (room.getType() != RoomType.Fairy && room.getType() != RoomType.Room ? 100 : 0);
                        if (newDist < dists[newR][newC]) {
                            dists[newR][newC] = newDist;
                            parent[newR][newC] = currentNode;
                            pq.add(new Node(newR, newC, newDist));
                        }
                    }
                }
            }
        }
        throw new IllegalStateException(start + " to " + end + " not possible");
    }

    private List<Node> reconstructPath(Node[][] parent, Node endNode) {
        List<Node> path = new ArrayList<Node>();
        Node current = endNode;
        while (current != null) {
            path.add(current);
            Node o = current;
            Pos2d oo = new Pos2d(current.row, current.col);
            current = parent[current.row][current.col];
            if (current != null) {
                Pos2d curr = new Pos2d(current.row, current.col);
                Pos2d diff = oo.sub(curr);
                if (diff.x() != 0) {
                    doorsHorizontal[curr.x() + (diff.x() > 0 ? 0 : -1)][curr.z()] = o == endNode ? DoorType.Fairy : DoorType.Wither;
                } else {
                    doorsVertical[curr.x()][curr.z() + (diff.z() > 0 ? 0 : -1)] = o == endNode ? DoorType.Fairy : DoorType.Wither;
                }
            }
        }
        Collections.reverse(path);
        return path;
    }

    private void fixSpecialRoomRotations() {
        for (int x = 0; x < rooms.length; x++) {
            for (int z = 0; z < rooms[x].length; z++) {
                Room room = rooms[x][z];
                if (room.getType() == RoomType.Puzzle) {
                    if (x != 0) {
                        if (doorsHorizontal[x - 1][z] == DoorType.Normal) {
                            rooms[x][z] = room.withRotation(Rotation.fromOffset(-1, 0));
                        }
                    } else if (x != doorsHorizontal.length) {
                        if (doorsHorizontal[x][z] == DoorType.Normal) {
                            rooms[x][z] = room.withRotation(Rotation.fromOffset(1, 0));
                        }
                    } else if (z != 0) {
                        if (doorsVertical[x][z - 1] == DoorType.Normal) {
                            rooms[x][z] = room.withRotation(Rotation.fromOffset(0, -1));
                        }
                    } else if (z != doorsVertical[x].length) {
                        if (doorsHorizontal[x][z] == DoorType.Normal) {
                            rooms[x][z] = room.withRotation(Rotation.fromOffset(0, 1));
                        }
                    }
                }
            }
        }
    }

    static class Node {
        int row;
        int col;
        int dist;

        public Node(int row, int col, int dist) {
            this.row = row;
            this.col = col;
            this.dist = dist;
        }
    }

    // Getters for doors and rooms
    public DoorType[][] getDoorsVertical() { return doorsVertical; }
    public DoorType[][] getDoorsHorizontal() { return doorsHorizontal; }
    public Room[][] getRooms() { return rooms; }
    public Room getEntrance() { return entrance; }
    public Room getBlood() { return blood; }
    public Room getFairy() { return fairy; }
    public Room getTrap() { return trap; }
    public Random getRandom() { return random; }
}