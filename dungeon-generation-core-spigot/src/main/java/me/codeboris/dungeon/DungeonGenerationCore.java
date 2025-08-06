package me.codeboris.dungeon;

import me.codeboris.dungeon.commands.DungeonCommand;
import me.codeboris.dungeon.schematic.SchematicLoader;
import me.codeboris.dungeon.world.DungeonWorldBuilder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Dungeon Generation Core
 * Hypixel-style dungeon generation system for Minecraft 1.8.8
 */
public class DungeonGenerationCore extends JavaPlugin {
    
    private static DungeonGenerationCore instance;
    private SchematicLoader schematicLoader;
    private DungeonWorldBuilder worldBuilder;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("=== Dungeon Generation Core ===");
        getLogger().info("Loading Hypixel-style dungeon generation...");
        
        // Initialize schematic system
        getLogger().info("Initializing schematic loader...");
        schematicLoader = new SchematicLoader(this);
        
        // Initialize world builder
        getLogger().info("Initializing world builder...");
        worldBuilder = new DungeonWorldBuilder(this, schematicLoader);
        
        // Register commands
        getCommand("dungeon").setExecutor(new DungeonCommand());
        
        getLogger().info("Dungeon Generation Core enabled successfully!");
        getLogger().info("Available schematic categories: " + schematicLoader.getAvailableCategories());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Dungeon Generation Core disabled!");
    }
    
    public static DungeonGenerationCore getInstance() {
        return instance;
    }
    
    public SchematicLoader getSchematicLoader() {
        return schematicLoader;
    }
    
    public DungeonWorldBuilder getWorldBuilder() {
        return worldBuilder;
    }
}