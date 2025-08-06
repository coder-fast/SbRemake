package me.carscupcake.dungeon.schematic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.carscupcake.dungeon.DungeonGenerationCore;
import me.carscupcake.dungeon.room.RoomShape;
import me.carscupcake.dungeon.room.RoomType;
import me.carscupcake.dungeon.schematic.SchematicFormat.SchematicBlock;
import me.carscupcake.dungeon.schematic.SchematicFormat.SchematicMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Loads schematics from the plugin folder structure
 * Supports: /plugins/DungeonGenerationCore/rooms/1x1/, /rooms/1x2/, etc.
 */
public class SchematicLoader {
    
    private final DungeonGenerationCore plugin;
    private final Map<String, List<SchematicData>> schematicCache;
    private final File roomsFolder;
    
    public SchematicLoader(DungeonGenerationCore plugin) {
        this.plugin = plugin;
        this.schematicCache = new HashMap<String, List<SchematicData>>();
        this.roomsFolder = new File(plugin.getDataFolder(), "rooms");
        
        // Create folder structure if it doesn't exist
        createFolderStructure();
        loadAllSchematics();
    }
    
    /**
     * Create the expected folder structure for room schematics
     */
    private void createFolderStructure() {
        if (!roomsFolder.exists()) {
            roomsFolder.mkdirs();
        }
        
        // Create folders for each room shape
        String[] shapes = {"1x1", "1x2", "1x3", "1x4", "L-shape", "2x2"};
        for (String shape : shapes) {
            File shapeFolder = new File(roomsFolder, shape);
            if (!shapeFolder.exists()) {
                shapeFolder.mkdirs();
                createExampleSchematic(shapeFolder, shape);
            }
        }
        
        // Create folders for special room types
        String[] specialTypes = {"entrance", "blood", "fairy", "trap", "puzzle"};
        for (String type : specialTypes) {
            File typeFolder = new File(roomsFolder, type);
            if (!typeFolder.exists()) {
                typeFolder.mkdirs();
                createExampleSchematic(typeFolder, type);
            }
        }
    }
    
    /**
     * Create example schematic files for reference
     */
    private void createExampleSchematic(File folder, String type) {
        File exampleFile = new File(folder, "example-" + type + ".json");
        if (!exampleFile.exists()) {
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(exampleFile));
                writer.println("// Example schematic for " + type);
                writer.println("// You can place your .schematic, .json, or .nbt files here");
                writer.println("// Supported formats:");
                writer.println("// - JSON format (like your original implementation)");
                writer.println("// - WorldEdit .schematic files");
                writer.println("// - Simple block lists");
                writer.close();
                
                plugin.getLogger().info("Created example file: " + exampleFile.getPath());
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create example file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Load all schematics from the folder structure
     */
    public void loadAllSchematics() {
        schematicCache.clear();
        
        if (!roomsFolder.exists()) {
            plugin.getLogger().warning("Rooms folder does not exist: " + roomsFolder.getPath());
            return;
        }
        
        int totalLoaded = 0;
        
        // Load room shape schematics
        for (RoomShape shape : RoomShape.values()) {
            String folderName = shape.toString();
            File shapeFolder = new File(roomsFolder, folderName);
            
            if (shapeFolder.exists() && shapeFolder.isDirectory()) {
                List<SchematicData> schematics = loadSchematicsFromFolder(shapeFolder, folderName);
                schematicCache.put(folderName, schematics);
                totalLoaded += schematics.size();
                
                plugin.getLogger().info("Loaded " + schematics.size() + " schematics for " + folderName);
            }
        }
        
        // Load special room type schematics
        for (RoomType type : RoomType.values()) {
            if (type == RoomType.ROOM) continue; // Regular rooms use shape-based schematics
            
            String folderName = type.name().toLowerCase();
            File typeFolder = new File(roomsFolder, folderName);
            
            if (typeFolder.exists() && typeFolder.isDirectory()) {
                List<SchematicData> schematics = loadSchematicsFromFolder(typeFolder, folderName);
                schematicCache.put(folderName, schematics);
                totalLoaded += schematics.size();
                
                plugin.getLogger().info("Loaded " + schematics.size() + " schematics for " + folderName);
            }
        }
        
        plugin.getLogger().info("Total schematics loaded: " + totalLoaded);
    }
    
    /**
     * Load schematics from a specific folder
     */
    private List<SchematicData> loadSchematicsFromFolder(File folder, String category) {
        List<SchematicData> schematics = new ArrayList<SchematicData>();
        
        File[] files = folder.listFiles();
        if (files == null) return schematics;
        
        for (File file : files) {
            if (file.isFile()) {
                SchematicData schematic = loadSchematicFile(file, category);
                if (schematic != null) {
                    schematics.add(schematic);
                }
            }
        }
        
        return schematics;
    }
    
    /**
     * Load a single schematic file
     */
    private SchematicData loadSchematicFile(File file, String category) {
        String fileName = file.getName().toLowerCase();
        
        try {
            if (fileName.endsWith(".json")) {
                return loadJsonSchematic(file, category);
            } else if (fileName.endsWith(".gz")) {
                return loadGzippedJsonSchematic(file, category);
            } else if (fileName.endsWith(".schematic")) {
                return loadWorldEditSchematic(file, category);
            } else {
                plugin.getLogger().warning("Unsupported file format: " + file.getName());
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load schematic " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Load JSON format schematic (like your original implementation)
     */
    private SchematicData loadJsonSchematic(File file, String category) throws IOException {
        FileReader reader = new FileReader(file);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        
        return parseJsonSchematic(json, file.getName(), category);
    }
    
    /**
     * Load gzipped JSON schematic
     */
    private SchematicData loadGzippedJsonSchematic(File file, String category) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        InputStreamReader reader = new InputStreamReader(gzis);
        
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        
        reader.close();
        gzis.close();
        fis.close();
        
        return parseJsonSchematic(json, file.getName(), category);
    }
    
    /**
     * Parse JSON schematic data
     */
    private SchematicData parseJsonSchematic(JsonObject json, String fileName, String category) {
        // Parse metadata
        String name = fileName.replace(".json", "").replace(".gz", "");
        String originRotation = json.has("originRotation") ? json.get("originRotation").getAsString() : "northwest";
        
        List<SchematicBlock> blocks = new ArrayList<SchematicBlock>();
        
        if (json.has("blocks")) {
            // Parse blocks array [x][y][z] format
            JsonArray xArray = json.getAsJsonArray("blocks");
            
            for (int x = 0; x < xArray.size(); x++) {
                JsonArray yArray = xArray.get(x).getAsJsonArray();
                for (int y = 0; y < yArray.size(); y++) {
                    JsonArray zArray = yArray.get(y).getAsJsonArray();
                    for (int z = 0; z < zArray.size(); z++) {
                        int blockId = zArray.get(z).getAsInt();
                        if (blockId != 0) { // Skip air blocks
                            Material material = getMaterialFromId(blockId);
                            blocks.add(new SchematicBlock(material, (byte) 0, x, y, z));
                        }
                    }
                }
            }
        }
        
        // Calculate dimensions
        int width = 32, height = 32, length = 32; // Default room size
        if (json.has("width")) width = json.get("width").getAsInt();
        if (json.has("height")) height = json.get("height").getAsInt();
        if (json.has("length")) length = json.get("length").getAsInt();
        
        SchematicMeta meta = new SchematicMeta(width, height, length, name, originRotation);
        return new SchematicData(meta, blocks, category);
    }
    
    /**
     * Load WorldEdit .schematic file (basic support)
     */
    private SchematicData loadWorldEditSchematic(File file, String category) {
        // TODO: Implement NBT reading for WorldEdit schematics
        // For now, create a placeholder
        plugin.getLogger().info("WorldEdit schematic support coming soon: " + file.getName());
        
        List<SchematicBlock> blocks = new ArrayList<SchematicBlock>();
        // Create a simple stone platform as placeholder
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                blocks.add(new SchematicBlock(Material.STONE, (byte) 0, x, 0, z));
            }
        }
        
        SchematicMeta meta = new SchematicMeta(32, 32, 32, file.getName(), "northwest");
        return new SchematicData(meta, blocks, category);
    }
    
    /**
     * Convert block ID to Material (1.8.8 compatibility)
     */
    @SuppressWarnings("deprecation")
    private Material getMaterialFromId(int id) {
        Material material = Material.getMaterial(id);
        return material != null ? material : Material.STONE;
    }
    
    /**
     * Get a random schematic for the given category
     */
    public SchematicData getRandomSchematic(String category) {
        List<SchematicData> schematics = schematicCache.get(category);
        if (schematics == null || schematics.isEmpty()) {
            plugin.getLogger().warning("No schematics found for category: " + category);
            return createDefaultSchematic(category);
        }
        
        Random random = new Random();
        return schematics.get(random.nextInt(schematics.size()));
    }
    
    /**
     * Get schematic by name
     */
    public SchematicData getSchematic(String category, String name) {
        List<SchematicData> schematics = schematicCache.get(category);
        if (schematics == null) return null;
        
        for (SchematicData schematic : schematics) {
            if (schematic.getMeta().getName().equals(name)) {
                return schematic;
            }
        }
        
        return null;
    }
    
    /**
     * Create a default schematic if none are found
     */
    private SchematicData createDefaultSchematic(String category) {
        List<SchematicBlock> blocks = new ArrayList<SchematicBlock>();
        
        // Create a basic room structure
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                // Floor
                blocks.add(new SchematicBlock(Material.STONE_SLAB, (byte) 0, x, 0, z));
                
                // Walls
                if (x == 0 || x == 31 || z == 0 || z == 31) {
                    for (int y = 1; y <= 4; y++) {
                        blocks.add(new SchematicBlock(Material.STONE_BRICKS, (byte) 0, x, y, z));
                    }
                }
            }
        }
        
        // Add some doors
        for (int y = 1; y <= 3; y++) {
            blocks.add(new SchematicBlock(Material.AIR, (byte) 0, 15, y, 0));  // North door
            blocks.add(new SchematicBlock(Material.AIR, (byte) 0, 15, y, 31)); // South door
            blocks.add(new SchematicBlock(Material.AIR, (byte) 0, 0, y, 15));  // West door
            blocks.add(new SchematicBlock(Material.AIR, (byte) 0, 31, y, 15)); // East door
        }
        
        SchematicMeta meta = new SchematicMeta(32, 32, 32, "default-" + category, "northwest");
        return new SchematicData(meta, blocks, category);
    }
    
    /**
     * Get available categories
     */
    public Set<String> getAvailableCategories() {
        return schematicCache.keySet();
    }
    
    /**
     * Get schematic count for category
     */
    public int getSchematicCount(String category) {
        List<SchematicData> schematics = schematicCache.get(category);
        return schematics != null ? schematics.size() : 0;
    }
    
    /**
     * Container for schematic data
     */
    public static class SchematicData {
        private final SchematicMeta meta;
        private final List<SchematicBlock> blocks;
        private final String category;
        
        public SchematicData(SchematicMeta meta, List<SchematicBlock> blocks, String category) {
            this.meta = meta;
            this.blocks = blocks;
            this.category = category;
        }
        
        public SchematicMeta getMeta() {
            return meta;
        }
        
        public List<SchematicBlock> getBlocks() {
            return blocks;
        }
        
        public String getCategory() {
            return category;
        }
    }
}