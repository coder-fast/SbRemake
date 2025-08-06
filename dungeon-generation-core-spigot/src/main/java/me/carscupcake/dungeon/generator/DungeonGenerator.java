package me.carscupcake.dungeon.generator;

import me.carscupcake.dungeon.room.*;
import me.carscupcake.dungeon.util.Pos2d;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Main dungeon generation algorithm
 * Based on Hypixel Skyblock Catacombs system
 */
public class DungeonGenerator {
    private static final int[][] DIRECTIONS = {
            {-1, 0}, // North
            {1, 0},  // South  
            {0, -1}, // West
            {0, 1}   // East
    };
    
    private final Random random;
    private final DoorType[][] doorsVertical;    // Vertical doors (North-South connections)
    private final DoorType[][] doorsHorizontal; // Horizontal doors (East-West connections)
    
    // Special rooms
    private final Room entrance;  // Green room (start)
    private final Room blood;     // Blood room (boss)
    private final Room fairy;     // Fairy room (miniboss)
    private final Room trap;      // Trap room
    
    private Room[][] rooms;
    
    public DungeonGenerator(Room[][] dimensions) {
        this(dimensions, System.currentTimeMillis());
    }
    
    public DungeonGenerator(Room[][] dimensions, long seed) {
        this.rooms = dimensions;
        this.doorsVertical = new DoorType[rooms.length][rooms[0].length - 1];
        this.doorsHorizontal = new DoorType[rooms.length - 1][rooms[0].length];
        this.random = new Random(seed);
        
        Bukkit.getLogger().info("Generating dungeon with seed: " + seed);
        
        // Validate dimensions
        int zLength = dimensions[0].length;
        for (int i = 0; i < dimensions.length; i++) {
            if (dimensions[i].length != zLength) {
                throw new IllegalArgumentException("Dimensions lengths don't match");
            }
        }
        
        // Create shuffled arrays for random placement
        Integer[] xCoords = new Integer[dimensions.length];
        Integer[] zCoords = new Integer[zLength];
        for (int i = 0; i < dimensions.length; i++) {
            xCoords[i] = i;
        }
        for (int i = 0; i < zLength; i++) {
            zCoords[i] = i;
        }
        shuffleArray(xCoords);
        shuffleArray(zCoords);
        
        // Place entrance (green room) - always on left edge
        Pos2d entrancePos = new Pos2d(0, random.nextInt(rooms[0].length));
        entrance = new Room(RoomType.ENTRANCE, RoomShape.ONE_BY_ONE, entrancePos, Rotation.NW, 
                Collections.<Pos2d>emptyList(), null);
        rooms[entrancePos.getX()][entrancePos.getZ()] = entrance;
        
        // Place blood room (boss room) - on right edge
        int redZ = random.nextInt(rooms[0].length);
        int redX = rooms.length - 1;
        if (redZ == 0 || redZ == rooms[0].length - 1) {
            if (random.nextBoolean()) {
                redX--;
            }
        }
        blood = new Room(RoomType.BLOOD, RoomShape.ONE_BY_ONE, new Pos2d(redX, redZ), Rotation.NW,
                Collections.<Pos2d>emptyList(), null);
        rooms[redX][redZ] = blood;
        
        // Place fairy room (miniboss) - in center area with space around it
        List<Pos2d> fairyPossibilities = new ArrayList<Pos2d>();
        for (int x = 1; x < rooms.length - 1; x++) {
            for (int z = 1; z < rooms[x].length - 1; z++) {
                if (testForFairy(x, z)) {
                    fairyPossibilities.add(new Pos2d(x, z));
                }
            }
        }
        Pos2d fairyPos = fairyPossibilities.get(random.nextInt(fairyPossibilities.size()));
        fairy = new Room(RoomType.FAIRY, RoomShape.ONE_BY_ONE, fairyPos, Rotation.NW,
                Collections.<Pos2d>emptyList(), null);
        rooms[fairyPos.getX()][fairyPos.getZ()] = fairy;
        
        // Place trap room - adjacent to fairy or entrance
        List<Pos2d> trapSpots = new ArrayList<Pos2d>(6);
        trapSpots.add(fairyPos.add(1, 0));
        trapSpots.add(fairyPos.add(0, 1));
        trapSpots.add(fairyPos.add(-1, 0));
        trapSpots.add(fairyPos.add(0, -1));
        if (entrancePos.getZ() != 0) trapSpots.add(entrancePos.add(0, -1));
        if (entrancePos.getZ() != rooms[0].length - 1) trapSpots.add(entrancePos.add(0, 1));
        
        Pos2d trapPos = trapSpots.get(random.nextInt(trapSpots.size()));
        trap = new Room(RoomType.TRAP, RoomShape.ONE_BY_ONE, trapPos, Rotation.NW,
                Collections.<Pos2d>emptyList(), null);
        rooms[trapPos.getX()][trapPos.getZ()] = trap;
        
        // Place puzzle rooms (2-4 puzzles)
        int puzzleAmount = 2 + random.nextInt(3);
        for (int i = 0; i < puzzleAmount; i++) {
            int x, z;
            do {
                x = random.nextInt(rooms.length);
                z = random.nextInt(rooms[0].length);
            } while (!canPlacePuzzle(x, z));
            
            rooms[x][z] = new Room(RoomType.PUZZLE, RoomShape.ONE_BY_ONE, new Pos2d(x, z), Rotation.NW,
                    Collections.<Pos2d>emptyList(), null);
        }
        
        // Fill remaining spaces with various room shapes
        for (Integer x : xCoords) {
            for (Integer z : zCoords) {
                tryPlaceRoom(new Pos2d(x, z));
            }
        }
        
        // Fill any remaining empty spots with 1x1 rooms
        for (Integer x : xCoords) {
            for (Integer z : zCoords) {
                if (rooms[x][z] == null) {
                    RoomShape.ONE_BY_ONE.tryInsert(rooms, new Pos2d(x, z), 
                            Rotation.values()[random.nextInt(4)]);
                }
            }
        }
    }
    
    /**
     * Check if we can place a puzzle room at this location
     */
    private boolean canPlacePuzzle(int x, int z) {
        if (rooms[x][z] != null) return false;
        // Puzzles should have space on sides to prevent clustering
        return (x + 1 == rooms.length || rooms[x + 1][z] == null) && 
               (x == 0 || rooms[x - 1][z] == null);
    }
    
    /**
     * Try to place various room shapes at the given position
     */
    private void tryPlaceRoom(Pos2d pos2d) {
        RoomShape[] shapes = {RoomShape.ONE_BY_TWO, RoomShape.ONE_BY_THREE, 
                             RoomShape.ONE_BY_FOUR, RoomShape.L_SHAPE, RoomShape.TWO_BY_TWO};
        shuffleArray(shapes);
        
        for (RoomShape shape : shapes) {
            Rotation rot = Rotation.values()[random.nextInt(4)];
            if (shape.tryInsert(rooms, pos2d, rot)) {
                // Set up bridge doors for multi-block rooms
                setupBridgeDoors(pos2d, shape, rot);
                return;
            }
        }
    }
    
    /**
     * Set up bridge doors for connecting parts of multi-block rooms
     */
    private void setupBridgeDoors(Pos2d pos2d, RoomShape shape, Rotation rot) {
        if (shape == RoomShape.TWO_BY_TWO) {
            // 2x2 rooms need bridge doors on all internal connections
            doorsHorizontal[pos2d.getX()][pos2d.getZ()] = DoorType.BRIDGE;
            doorsVertical[pos2d.getX()][pos2d.getZ()] = DoorType.BRIDGE;
            doorsHorizontal[pos2d.getX()][pos2d.getZ() + 1] = DoorType.BRIDGE;
            doorsVertical[pos2d.getX() + 1][pos2d.getZ()] = DoorType.BRIDGE;
        } else {
            // Handle linear and L-shaped rooms
            Pos2d prev = null;
            for (Pos2d child : shape.getChildren(pos2d, rot)) {
                if (shape == RoomShape.ONE_BY_TWO || shape == RoomShape.ONE_BY_THREE || 
                    shape == RoomShape.ONE_BY_FOUR) {
                    // Linear rooms
                    if (rot == Rotation.NE || rot == Rotation.SW) {
                        doorsVertical[child.getX()][child.getZ() - 1] = DoorType.BRIDGE;
                    } else if (rot == Rotation.SE || rot == Rotation.NW) {
                        doorsHorizontal[child.getX() - 1][child.getZ()] = DoorType.BRIDGE;
                    }
                } else if (shape == RoomShape.L_SHAPE) {
                    // L-shaped rooms
                    Pos2d offset = prev == null ? pos2d : prev;
                    Pos2d diff = child.sub(offset);
                    if (diff.getX() != 0) {
                        doorsHorizontal[offset.getX() + (diff.getX() > 0 ? 0 : -1)][offset.getZ()] = DoorType.BRIDGE;
                    } else {
                        doorsVertical[offset.getX()][offset.getZ() + (diff.getZ() > 0 ? 0 : -1)] = DoorType.BRIDGE;
                    }
                    prev = child;
                }
            }
        }
    }
    
    /**
     * Test if fairy room can be placed (needs empty space around it)
     */
    private boolean testForFairy(int x, int z) {
        return rooms[x + 1][z] == null && rooms[x][z + 1] == null &&
               rooms[x - 1][z] == null && rooms[x][z - 1] == null;
    }
    
    /**
     * Fisher-Yates shuffle algorithm
     */
    private <T> void shuffleArray(T[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            T temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
    
    /**
     * Generate door connections using tree exploration
     * Starting from the fairy room as root
     */
    public void generateDoors(Pos2d start) {
        boolean[][] discovered = new boolean[rooms.length][rooms[0].length];
        Queue<Pos2d> possibilities = new LinkedList<Pos2d>();
        possibilities.add(start);
        
        // Tree-based room exploration
        while (!possibilities.isEmpty()) {
            Pos2d currentPos = possibilities.poll();
            Room current = getFromPos(currentPos);
            
            if (current.getType() != RoomType.ROOM && current.getType() != RoomType.FAIRY) {
                if (current.getType() == RoomType.ENTRANCE) {
                    doorsHorizontal[0][current.getPosition().getZ()] = DoorType.START;
                }
                continue;
            }
            
            // Check all four directions for connections
            if (testPosition(current.getPosition(), 1, 0, discovered)) {
                addDoor(current.getPosition().add(1, 0), possibilities, discovered);
                doorsHorizontal[current.getPosition().getX()][current.getPosition().getZ()] = DoorType.NORMAL;
            }
            if (testPosition(current.getPosition(), -1, 0, discovered)) {
                addDoor(current.getPosition().add(-1, 0), possibilities, discovered);
                Room leftRoom = getFromPos(current.getPosition().add(-1, 0));
                doorsHorizontal[current.getPosition().getX() - 1][current.getPosition().getZ()] = 
                        leftRoom.getType() == RoomType.ENTRANCE ? DoorType.START : DoorType.NORMAL;
            }
            if (testPosition(current.getPosition(), 0, 1, discovered)) {
                addDoor(current.getPosition().add(0, 1), possibilities, discovered);
                doorsVertical[current.getPosition().getX()][current.getPosition().getZ()] = DoorType.NORMAL;
            }
            if (testPosition(current.getPosition(), 0, -1, discovered)) {
                addDoor(current.getPosition().add(0, -1), possibilities, discovered);
                doorsVertical[current.getPosition().getX()][current.getPosition().getZ() - 1] = DoorType.NORMAL;
            }
        }
        
        // Apply Dijkstra pathfinding for critical paths
        dijkstraPath(entrance.getPosition(), fairy.getPosition());
        dijkstraPath(fairy.getPosition(), blood.getPosition());
        
        // Fix special room rotations based on door placement
        fixSpecialRoomRotations();
    }
    
    private void addDoor(Pos2d targetPos, Queue<Pos2d> possibilities, boolean[][] discovered) {
        discovered[targetPos.getX()][targetPos.getZ()] = true;
        Room target = getFromPos(targetPos);
        
        if (target.getParent() != null) {
            target = getFromPos(target.getParent());
        }
        
        for (Pos2d childPos : target.getChildren()) {
            if (!possibilities.contains(childPos)) {
                possibilities.add(childPos);
            }
            discovered[childPos.getX()][childPos.getZ()] = true;
        }
        
        if (!possibilities.contains(target.getPosition())) {
            possibilities.add(target.getPosition());
        }
    }
    
    private boolean testPosition(Pos2d pos2d, int x, int z, boolean[][] discovered) {
        if (pos2d.getX() + x < 0 || pos2d.getZ() + z < 0 || 
            pos2d.getX() + x >= rooms.length || pos2d.getZ() + z >= rooms[0].length) {
            return false;
        }
        
        Room door = getFromPos(pos2d.add(x, z));
        if (door.getType() == RoomType.ENTRANCE) {
            if (x != -1) return false; // Entrance only accessible from the right
        }
        
        return !discovered[pos2d.getX() + x][pos2d.getZ() + z];
    }
    
    /**
     * Dijkstra pathfinding algorithm for critical paths (Green→Fairy→Blood)
     */
    public void dijkstraPath(Pos2d start, Pos2d end) {
        int[][] distances = new int[rooms.length][rooms[0].length];
        int numRows = rooms.length;
        int numCols = rooms[0].length;
        
        // Initialize distances
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                distances[i][j] = Integer.MAX_VALUE;
            }
        }
        
        Node[][] parent = new Node[numRows][numCols];
        PriorityQueue<Node> pq = new PriorityQueue<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node a, Node b) {
                return Integer.compare(a.distance, b.distance);
            }
        });
        
        distances[start.getX()][start.getZ()] = 0;
        pq.add(new Node(start.getX(), start.getZ(), 0));
        
        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            int r = currentNode.row;
            int c = currentNode.col;
            int currentDist = currentNode.distance;
            
            if (currentDist > distances[r][c]) {
                continue;
            }
            
            if (r == end.getX() && c == end.getZ()) {
                reconstructPath(parent, new Node(end.getX(), end.getZ(), currentDist));
                return;
            }
            
            // Check all four directions
            for (int[] dir : DIRECTIONS) {
                int newR = r + dir[0];
                int newC = c + dir[1];
                
                if (newR >= 0 && newR < numRows && newC >= 0 && newC < numCols) {
                    boolean canMove = false;
                    
                    // Check door availability
                    if (dir[0] == -1) { // Moving North
                        if (r > 0) {
                            DoorType type = doorsHorizontal[r - 1][c];
                            canMove = type == DoorType.NORMAL || type == DoorType.BRIDGE;
                        }
                    } else if (dir[0] == 1) { // Moving South
                        if (r < numRows - 1) {
                            DoorType type = doorsHorizontal[r][c];
                            canMove = type == DoorType.NORMAL || type == DoorType.START || type == DoorType.BRIDGE;
                        }
                    } else if (dir[1] == -1) { // Moving West
                        if (c > 0) {
                            DoorType type = doorsVertical[r][c - 1];
                            canMove = type == DoorType.NORMAL || type == DoorType.BRIDGE;
                        }
                    } else if (dir[1] == 1) { // Moving East
                        if (c < numCols - 1) {
                            DoorType type = doorsVertical[r][c];
                            canMove = type == DoorType.NORMAL || type == DoorType.BRIDGE;
                        }
                    }
                    
                    if (canMove) {
                        Room room = rooms[newR][newC];
                        // Add penalty for non-room types to prefer going through regular rooms
                        int newDist = currentDist + 1 + 
                                (room.getType() != RoomType.FAIRY && room.getType() != RoomType.ROOM ? 100 : 0);
                        
                        if (newDist < distances[newR][newC]) {
                            distances[newR][newC] = newDist;
                            parent[newR][newC] = currentNode;
                            pq.add(new Node(newR, newC, newDist));
                        }
                    }
                }
            }
        }
        
        throw new IllegalStateException("Path from " + start + " to " + end + " not possible");
    }
    
    /**
     * Reconstruct and mark the critical path with special door types
     */
    private List<Node> reconstructPath(Node[][] parent, Node endNode) {
        List<Node> path = new ArrayList<Node>();
        Node current = endNode;
        
        while (current != null) {
            path.add(current);
            Node previous = current;
            current = parent[current.row][current.col];
            
            if (current != null) {
                Pos2d curr = new Pos2d(current.row, current.col);
                Pos2d prev = new Pos2d(previous.row, previous.col);
                Pos2d diff = prev.sub(curr);
                
                // Mark critical path doors
                if (diff.getX() != 0) {
                    doorsHorizontal[curr.getX() + (diff.getX() > 0 ? 0 : -1)][curr.getZ()] = 
                            previous == endNode ? DoorType.FAIRY : DoorType.WITHER;
                } else {
                    doorsVertical[curr.getX()][curr.getZ() + (diff.getZ() > 0 ? 0 : -1)] = 
                            previous == endNode ? DoorType.FAIRY : DoorType.WITHER;
                }
            }
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Fix rotations of special rooms based on door connections
     */
    private void fixSpecialRoomRotations() {
        for (int x = 0; x < rooms.length; x++) {
            for (int z = 0; z < rooms[x].length; z++) {
                Room room = rooms[x][z];
                if (room.getType() == RoomType.PUZZLE) {
                    // Orient puzzle rooms towards their entrance door
                    if (x != 0 && doorsHorizontal[x - 1][z] == DoorType.NORMAL) {
                        rooms[x][z] = room.withRotation(Rotation.fromOffset(-1, 0));
                    } else if (x != doorsHorizontal.length && doorsHorizontal[x][z] == DoorType.NORMAL) {
                        rooms[x][z] = room.withRotation(Rotation.fromOffset(1, 0));
                    } else if (z != 0 && doorsVertical[x][z - 1] == DoorType.NORMAL) {
                        rooms[x][z] = room.withRotation(Rotation.fromOffset(0, -1));
                    } else if (z != doorsVertical[x].length && doorsVertical[x][z] == DoorType.NORMAL) {
                        rooms[x][z] = room.withRotation(Rotation.fromOffset(0, 1));
                    }
                }
            }
        }
    }
    
    // Getters
    public Room getFromPos(Pos2d pos2d) {
        return rooms[pos2d.getX()][pos2d.getZ()];
    }
    
    public Room[][] getRooms() {
        return rooms;
    }
    
    public DoorType[][] getDoorsVertical() {
        return doorsVertical;
    }
    
    public DoorType[][] getDoorsHorizontal() {
        return doorsHorizontal;
    }
    
    public Room getEntrance() {
        return entrance;
    }
    
    public Room getBlood() {
        return blood;
    }
    
    public Room getFairy() {
        return fairy;
    }
    
    public Room getTrap() {
        return trap;
    }
    
    public Random getRandom() {
        return random;
    }
    
    /**
     * Node class for Dijkstra pathfinding
     */
    static class Node {
        int row;
        int col;
        int distance;
        
        public Node(int row, int col, int distance) {
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }
}