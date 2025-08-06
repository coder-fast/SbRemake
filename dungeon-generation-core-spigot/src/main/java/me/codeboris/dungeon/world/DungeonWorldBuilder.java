package me.codeboris.dungeon.world;

import me.codeboris.dungeon.DungeonGenerationCore;
import me.codeboris.dungeon.generator.DungeonGenerator;
import me.codeboris.dungeon.room.*;
import me.codeboris.dungeon.schematic.SchematicLoader;
import me.codeboris.dungeon.schematic.SchematicLoader.SchematicData;
import me.codeboris.dungeon.schematic.SchematicFormat.SchematicBlock;
import me.codeboris.dungeon.util.Pos2d;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds the actual dungeon world from generator data
 * Converts room data to blocks using schematics
 */
public class DungeonWorldBuilder {
    
    private final DungeonGenerationCore plugin;
    private final SchematicLoader schematicLoader;
    
    public DungeonWorldBuilder(DungeonGenerationCore plugin, SchematicLoader schematicLoader) {
        this.plugin = plugin;
        this.schematicLoader = schematicLoader;
    }
    
    /**
     * Build the complete dungeon in the world
     */
    public void buildDungeon(DungeonGenerator generator, World world, Location startLocation) {
        plugin.getLogger().info("Starting dungeon construction...");
        
        // Calculate base coordinates
        int baseX = startLocation.getBlockX();
        int baseY = startLocation.getBlockY();
        int baseZ = startLocation.getBlockZ();
        
        // Track progress
        AtomicInteger roomsBuilt = new AtomicInteger(0);
        AtomicInteger totalRooms = new AtomicInteger(0);
        
        // Count total rooms to build
        for (Room[] row : generator.getRooms()) {
            for (Room room : row) {
                if (room != null && room.getParent() == null) { // Only count parent rooms
                    totalRooms.incrementAndGet();
                }
            }
        }
        
        plugin.getLogger().info("Building " + totalRooms.get() + " rooms...");
        
        // Build rooms asynchronously to prevent server lag
        new BukkitRunnable() {
            private int currentX = 0;
            private int currentZ = 0;
            
            @Override
            public void run() {
                // Build one room per tick to prevent lag
                if (currentX < generator.getRooms().length) {
                    if (currentZ < generator.getRooms()[currentX].length) {
                        Room room = generator.getRooms()[currentX][currentZ];
                        
                        if (room != null && room.getParent() == null) {
                            buildRoom(room, generator, world, baseX, baseY, baseZ);
                            roomsBuilt.incrementAndGet();
                            
                            plugin.getLogger().info("Built room " + roomsBuilt.get() + "/" + totalRooms.get() + 
                                    " - " + room.getType() + " at " + room.getPosition());
                        }
                        
                        currentZ++;
                    } else {
                        currentX++;
                        currentZ = 0;
                    }
                } else {
                    // All rooms built, now build doors
                    buildDoors(generator, world, baseX, baseY, baseZ);
                    plugin.getLogger().info("Dungeon construction completed!");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 2L); // Run every 2 ticks
    }
    
    /**
     * Build a single room using schematics
     */
    private void buildRoom(Room room, DungeonGenerator generator, World world, int baseX, int baseY, int baseZ) {
        // Get the appropriate schematic
        SchematicData schematic = getSchematicForRoom(room);
        if (schematic == null) {
            plugin.getLogger().warning("No schematic found for room: " + room.getType() + " " + room.getShape());
            return;
        }
        
        // Calculate room position in world coordinates
        int roomX = baseX + (room.getPosition().getX() * 32);
        int roomY = baseY;
        int roomZ = baseZ + (room.getPosition().getZ() * 32);
        
        // Clear the area first
        clearRoomArea(world, roomX, roomY, roomZ, room.getShape());
        
        // Apply rotation offset
        Rotation roomRotation = room.getRotation();
        String schematicRotation = schematic.getMeta().getOriginRotation();
        int rotationDifference = calculateRotationDifference(roomRotation, schematicRotation);
        
        // Place blocks from schematic
        for (SchematicBlock schematicBlock : schematic.getBlocks()) {
            // Apply rotation
            int[] rotatedCoords = applyRotation(schematicBlock.getX(), schematicBlock.getZ(), 
                    rotationDifference, room.getShape());
            
            int worldX = roomX + rotatedCoords[0];
            int worldY = roomY + schematicBlock.getY();
            int worldZ = roomZ + rotatedCoords[1];
            
            // Place the block
            Block block = world.getBlockAt(worldX, worldY, worldZ);
            schematicBlock.applyToWorld(block);
        }
        
        // Handle multi-block rooms
        buildChildRooms(room, generator, world, baseX, baseY, baseZ, schematic);
    }
    
    /**
     * Build child rooms for multi-block shapes
     */
    private void buildChildRooms(Room parentRoom, DungeonGenerator generator, World world, 
                                int baseX, int baseY, int baseZ, SchematicData schematic) {
        for (Pos2d childPos : parentRoom.getChildren()) {
            Room childRoom = generator.getFromPos(childPos);
            if (childRoom != null) {
                // Calculate child room position
                int roomX = baseX + (childPos.getX() * 32);
                int roomY = baseY;
                int roomZ = baseZ + (childPos.getZ() * 32);
                
                // Clear child area
                clearRoomArea(world, roomX, roomY, roomZ, RoomShape.ONE_BY_ONE);
                
                // Place schematic blocks for child room (with offset)
                int offsetX = (childPos.getX() - parentRoom.getPosition().getX()) * 32;
                int offsetZ = (childPos.getZ() - parentRoom.getPosition().getZ()) * 32;
                
                for (SchematicBlock schematicBlock : schematic.getBlocks()) {
                    int relativeX = schematicBlock.getX() - offsetX;
                    int relativeZ = schematicBlock.getZ() - offsetZ;
                    
                    // Check if block belongs to this child room
                    if (relativeX >= 0 && relativeX < 32 && relativeZ >= 0 && relativeZ < 32) {
                        int worldX = roomX + relativeX;
                        int worldY = roomY + schematicBlock.getY();
                        int worldZ = roomZ + relativeZ;
                        
                        Block block = world.getBlockAt(worldX, worldY, worldZ);
                        schematicBlock.applyToWorld(block);
                    }
                }
            }
        }
    }
    
    /**
     * Build doors between rooms
     */
    private void buildDoors(DungeonGenerator generator, World world, int baseX, int baseY, int baseZ) {
        plugin.getLogger().info("Building doors...");
        
        // Build horizontal doors (East-West connections)
        for (int x = 0; x < generator.getDoorsHorizontal().length; x++) {
            for (int z = 0; z < generator.getDoorsHorizontal()[x].length; z++) {
                DoorType doorType = generator.getDoorsHorizontal()[x][z];
                if (doorType != null && doorType != DoorType.NONE) {
                    buildHorizontalDoor(world, baseX, baseY, baseZ, x, z, doorType);
                }
            }
        }
        
        // Build vertical doors (North-South connections)
        for (int x = 0; x < generator.getDoorsVertical().length; x++) {
            for (int z = 0; z < generator.getDoorsVertical()[x].length; z++) {
                DoorType doorType = generator.getDoorsVertical()[x][z];
                if (doorType != null && doorType != DoorType.NONE) {
                    buildVerticalDoor(world, baseX, baseY, baseZ, x, z, doorType);
                }
            }
        }
    }
    
    /**
     * Build a horizontal door (East-West connection)
     */
    private void buildHorizontalDoor(World world, int baseX, int baseY, int baseZ, 
                                   int gridX, int gridZ, DoorType doorType) {
        // Door is on the right edge of the room at gridX, gridZ
        int doorX = baseX + (gridX * 32) + 31; // Right edge
        int doorY = baseY;
        int doorZ = baseZ + (gridZ * 32) + 15; // Center of room
        
        Material doorMaterial = getDoorMaterial(doorType);
        
        // Create door opening (3 blocks high, 2 blocks wide)
        for (int y = 1; y <= 3; y++) {
            for (int z = 0; z < 2; z++) {
                Block block = world.getBlockAt(doorX, doorY + y, doorZ + z);
                block.setType(Material.AIR);
                
                // Add door frame
                if (y == 1) {
                    Block frameBlock = world.getBlockAt(doorX, doorY + y - 1, doorZ + z);
                    frameBlock.setType(doorMaterial);
                }
            }
        }
        
        // Add special door markers
        if (doorType == DoorType.WITHER || doorType == DoorType.FAIRY) {
            Block markerBlock = world.getBlockAt(doorX, doorY + 2, doorZ + 1);
            markerBlock.setType(doorType == DoorType.WITHER ? Material.OBSIDIAN : Material.QUARTZ_BLOCK);
        }
    }
    
    /**
     * Build a vertical door (North-South connection)
     */
    private void buildVerticalDoor(World world, int baseX, int baseY, int baseZ, 
                                 int gridX, int gridZ, DoorType doorType) {
        // Door is on the bottom edge of the room at gridX, gridZ
        int doorX = baseX + (gridX * 32) + 15; // Center of room
        int doorY = baseY;
        int doorZ = baseZ + (gridZ * 32) + 31; // Bottom edge
        
        Material doorMaterial = getDoorMaterial(doorType);
        
        // Create door opening (3 blocks high, 2 blocks wide)
        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < 2; x++) {
                Block block = world.getBlockAt(doorX + x, doorY + y, doorZ);
                block.setType(Material.AIR);
                
                // Add door frame
                if (y == 1) {
                    Block frameBlock = world.getBlockAt(doorX + x, doorY + y - 1, doorZ);
                    frameBlock.setType(doorMaterial);
                }
            }
        }
        
        // Add special door markers
        if (doorType == DoorType.WITHER || doorType == DoorType.FAIRY) {
            Block markerBlock = world.getBlockAt(doorX + 1, doorY + 2, doorZ);
            markerBlock.setType(doorType == DoorType.WITHER ? Material.OBSIDIAN : Material.QUARTZ_BLOCK);
        }
    }
    
    /**
     * Get door material based on door type
     */
    private Material getDoorMaterial(DoorType doorType) {
        switch (doorType) {
            case START:
                return Material.EMERALD_BLOCK;
            case WITHER:
                return Material.COAL_BLOCK;
            case FAIRY:
                return Material.QUARTZ_BLOCK;
            case BRIDGE:
                return Material.STONE;
            case NORMAL:
            default:
                return Material.STONE_BRICKS;
        }
    }
    
    /**
     * Clear the room area before building
     */
    private void clearRoomArea(World world, int x, int y, int z, RoomShape shape) {
        // Clear a 32x32x32 area
        for (int dx = 0; dx < 32; dx++) {
            for (int dy = 0; dy < 32; dy++) {
                for (int dz = 0; dz < 32; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    block.setType(Material.AIR);
                }
            }
        }
    }
    
    /**
     * Get appropriate schematic for a room
     */
    private SchematicData getSchematicForRoom(Room room) {
        String category;
        
        if (room.getType() == RoomType.ROOM) {
            // Use room shape for regular rooms
            category = room.getShape().toString();
        } else {
            // Use room type for special rooms
            category = room.getType().name().toLowerCase();
        }
        
        return schematicLoader.getRandomSchematic(category);
    }
    
    /**
     * Calculate rotation difference between room and schematic
     */
    private int calculateRotationDifference(Rotation roomRotation, String schematicRotation) {
        Rotation schematicRot;
        try {
            schematicRot = Rotation.fromName(schematicRotation);
        } catch (IllegalArgumentException e) {
            schematicRot = Rotation.NW; // Default
        }
        
        int roomOrdinal = roomRotation.ordinal();
        int schematicOrdinal = schematicRot.ordinal();
        
        int difference = roomOrdinal - schematicOrdinal;
        if (difference < 0) difference += 4;
        
        return difference;
    }
    
    /**
     * Apply rotation to coordinates
     */
    private int[] applyRotation(int x, int z, int rotation, RoomShape shape) {
        int[] coords = {x, z};
        
        for (int i = 0; i < rotation; i++) {
            int newX = 31 - coords[1]; // 90-degree clockwise rotation
            int newZ = coords[0];
            coords[0] = newX;
            coords[1] = newZ;
        }
        
        return coords;
    }
    
    /**
     * Build dungeon instantly (for testing)
     */
    public void buildDungeonInstant(DungeonGenerator generator, World world, Location startLocation) {
        plugin.getLogger().info("Building dungeon instantly...");
        
        int baseX = startLocation.getBlockX();
        int baseY = startLocation.getBlockY();
        int baseZ = startLocation.getBlockZ();
        
        // Build all rooms
        for (Room[] row : generator.getRooms()) {
            for (Room room : row) {
                if (room != null && room.getParent() == null) {
                    buildRoom(room, generator, world, baseX, baseY, baseZ);
                }
            }
        }
        
        // Build doors
        buildDoors(generator, world, baseX, baseY, baseZ);
        
        plugin.getLogger().info("Instant dungeon construction completed!");
    }
}