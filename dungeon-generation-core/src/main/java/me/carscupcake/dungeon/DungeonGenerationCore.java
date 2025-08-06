package me.carscupcake.dungeon;

import me.carscupcake.dungeon.commands.DungeonCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Dungeon Generation Core
 * Hypixel-style dungeon generation system for Minecraft 1.8.8
 */
public class DungeonGenerationCore extends JavaPlugin {
    
    private static DungeonGenerationCore instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("=== Dungeon Generation Core ===");
        getLogger().info("Loading Hypixel-style dungeon generation...");
        
        // Register commands
        getCommand("dungeon").setExecutor(new DungeonCommand());
        
        getLogger().info("Dungeon Generation Core enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Dungeon Generation Core disabled!");
    }
    
    public static DungeonGenerationCore getInstance() {
        return instance;
    }
}