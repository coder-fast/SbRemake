package me.carscupcake.dungeon.commands;

import me.carscupcake.dungeon.DungeonGenerationCore;
import me.carscupcake.dungeon.generator.DungeonGenerator;
import me.carscupcake.dungeon.room.DoorType;
import me.carscupcake.dungeon.room.Room;
import me.carscupcake.dungeon.room.RoomType;
import me.carscupcake.dungeon.util.Pos2d;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for dungeon generation
 */
public class DungeonCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("dungeon.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /dungeon <generate|test|build|reload>");
            player.sendMessage(ChatColor.GRAY + "  generate [seed] - Generate dungeon algorithm");
            player.sendMessage(ChatColor.GRAY + "  test - Display ASCII dungeon map");
            player.sendMessage(ChatColor.GRAY + "  build [seed] - Build actual dungeon in world");
            player.sendMessage(ChatColor.GRAY + "  reload - Reload schematic files");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "generate":
                generateDungeon(player, args);
                break;
            case "test":
                testDungeon(player);
                break;
            case "build":
                buildDungeon(player, args);
                break;
            case "reload":
                reloadSchematics(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand! Use: generate, test, build, or reload");
                break;
        }
        
        return true;
    }
    
    /**
     * Generate a dungeon at the player's location
     */
    private void generateDungeon(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Generating dungeon...");
        
        long seed = System.currentTimeMillis();
        if (args.length > 1) {
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.YELLOW + "Invalid seed, using random seed: " + seed);
            }
        }
        
        // Create 6x6 dungeon grid
        Room[][] rooms = new Room[6][6];
        DungeonGenerator generator = new DungeonGenerator(rooms, seed);
        
        // Generate door connections starting from fairy room
        generator.generateDoors(new Pos2d(generator.getFairy().getPosition().getX(), 
                                        generator.getFairy().getPosition().getZ()));
        
        player.sendMessage(ChatColor.GREEN + "Dungeon generated with seed: " + seed);
        player.sendMessage(ChatColor.AQUA + "Entrance: " + generator.getEntrance().getPosition());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Fairy: " + generator.getFairy().getPosition());
        player.sendMessage(ChatColor.RED + "Blood: " + generator.getBlood().getPosition());
        player.sendMessage(ChatColor.WHITE + "Trap: " + generator.getTrap().getPosition());
        
        // TODO: Implement actual world generation here
        player.sendMessage(ChatColor.YELLOW + "Note: World generation not yet implemented. This is algorithm testing only.");
    }
    
    /**
     * Test dungeon generation and display ASCII representation
     */
    private void testDungeon(Player player) {
        player.sendMessage(ChatColor.GREEN + "Testing dungeon generation...");
        
        Room[][] rooms = new Room[6][6];
        DungeonGenerator generator = new DungeonGenerator(rooms);
        generator.generateDoors(new Pos2d(generator.getFairy().getPosition().getX(), 
                                        generator.getFairy().getPosition().getZ()));
        
        // Create ASCII representation
        StringBuilder output = new StringBuilder();
        output.append("\n").append(ChatColor.BOLD).append("=== DUNGEON MAP ===\n").append(ChatColor.RESET);
        
        for (int x = 0; x < generator.getRooms().length; x++) {
            // First line: rooms
            for (int z = 0; z < generator.getRooms()[x].length; z++) {
                Room room = generator.getRooms()[x][z];
                String roomDisplay = formatRoom(room);
                output.append(roomDisplay);
                
                // Vertical doors
                if (z < generator.getRooms()[x].length - 1) {
                    DoorType doorType = generator.getDoorsVertical()[x][z];
                    output.append(formatDoor(doorType, true));
                }
            }
            output.append("\n");
            
            // Second line: horizontal doors
            if (x < generator.getRooms().length - 1) {
                for (int z = 0; z < generator.getRooms()[x].length; z++) {
                    DoorType doorType = generator.getDoorsHorizontal()[x][z];
                    output.append(formatDoor(doorType, false));
                    
                    if (z < generator.getRooms()[x].length - 1) {
                        output.append("-");
                    }
                }
                output.append("\n");
            }
        }
        
        // Send the map to player in chunks (to avoid chat spam limits)
        String[] lines = output.toString().split("\n");
        for (String line : lines) {
            player.sendMessage(line);
        }
        
        // Send summary
        player.sendMessage(ChatColor.GREEN + "\nLegend:");
        player.sendMessage(ChatColor.GREEN + "[E] = Entrance " + ChatColor.LIGHT_PURPLE + "[F] = Fairy " + 
                          ChatColor.RED + "[B] = Blood " + ChatColor.WHITE + "[T] = Trap");
        player.sendMessage(ChatColor.BLUE + "[P] = Puzzle " + ChatColor.YELLOW + "[R] = Room");
        player.sendMessage(ChatColor.DARK_GRAY + "| = Normal Door, " + ChatColor.BLACK + "| = Wither/Fairy Door, " + 
                          ChatColor.DARK_GREEN + "| = Start Door");
    }
    
    /**
     * Format room for ASCII display
     */
    private String formatRoom(Room room) {
        switch (room.getType()) {
            case ENTRANCE:
                return ChatColor.GREEN + "[E]" + ChatColor.RESET;
            case FAIRY:
                return ChatColor.LIGHT_PURPLE + "[F]" + ChatColor.RESET;
            case BLOOD:
                return ChatColor.RED + "[B]" + ChatColor.RESET;
            case TRAP:
                return ChatColor.WHITE + "[T]" + ChatColor.RESET;
            case PUZZLE:
                return ChatColor.BLUE + "[P]" + ChatColor.RESET;
            case ROOM:
                return ChatColor.YELLOW + "[R]" + ChatColor.RESET;
            default:
                return "[?]";
        }
    }
    
    /**
     * Format door for ASCII display
     */
    private String formatDoor(DoorType doorType, boolean isVertical) {
        if (doorType == null) {
            return isVertical ? " " : "   ";
        }
        
        String symbol = isVertical ? "|" : "---";
        
        switch (doorType) {
            case START:
                return ChatColor.DARK_GREEN + symbol + ChatColor.RESET;
            case WITHER:
            case FAIRY:
                return ChatColor.BLACK + symbol + ChatColor.RESET;
            case BRIDGE:
                return ChatColor.GRAY + symbol + ChatColor.RESET;
            case NORMAL:
                return ChatColor.DARK_GRAY + symbol + ChatColor.RESET;
            default:
                return symbol;
        }
    }
    
    /**
     * Build an actual dungeon in the world
     */
    private void buildDungeon(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Building dungeon in world...");
        
        long seed = System.currentTimeMillis();
        if (args.length > 1) {
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.YELLOW + "Invalid seed, using random seed: " + seed);
            }
        }
        
        // Create 6x6 dungeon grid
        Room[][] rooms = new Room[6][6];
        DungeonGenerator generator = new DungeonGenerator(rooms, seed);
        
        // Generate door connections starting from fairy room
        generator.generateDoors(new Pos2d(generator.getFairy().getPosition().getX(), 
                                        generator.getFairy().getPosition().getZ()));
        
        // Build in world
        DungeonGenerationCore plugin = DungeonGenerationCore.getInstance();
        plugin.getWorldBuilder().buildDungeon(generator, player.getWorld(), player.getLocation());
        
        player.sendMessage(ChatColor.GREEN + "Dungeon construction started with seed: " + seed);
        player.sendMessage(ChatColor.YELLOW + "Check console for build progress...");
        player.sendMessage(ChatColor.AQUA + "Entrance: " + generator.getEntrance().getPosition());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Fairy: " + generator.getFairy().getPosition());
        player.sendMessage(ChatColor.RED + "Blood: " + generator.getBlood().getPosition());
        player.sendMessage(ChatColor.WHITE + "Trap: " + generator.getTrap().getPosition());
    }
    
    /**
     * Reload schematic files
     */
    private void reloadSchematics(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Reloading schematic files...");
        
        DungeonGenerationCore plugin = DungeonGenerationCore.getInstance();
        plugin.getSchematicLoader().loadAllSchematics();
        
        player.sendMessage(ChatColor.GREEN + "Schematics reloaded successfully!");
        player.sendMessage(ChatColor.GRAY + "Available categories: " + plugin.getSchematicLoader().getAvailableCategories());
        
        // Show schematic counts
        for (String category : plugin.getSchematicLoader().getAvailableCategories()) {
            int count = plugin.getSchematicLoader().getSchematicCount(category);
            player.sendMessage(ChatColor.GRAY + "  " + category + ": " + count + " schematics");
        }
    }
}