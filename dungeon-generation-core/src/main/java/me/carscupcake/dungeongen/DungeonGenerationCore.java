package me.carscupcake.dungeongen;

import org.bukkit.plugin.java.JavaPlugin;

public class DungeonGenerationCore extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("DungeonGenerationCore enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DungeonGenerationCore disabled!");
    }
}