# Dungeon Generation Core - Spigot Plugin

## 🏰 Complete Hypixel Catacombs System for Spigot 1.8.8

This repository now includes a **complete, production-ready** Hypixel-style dungeon generation plugin ported from the original Minestom implementation to Spigot 1.8.8.

## 📁 Location

**`/dungeon-generation-core-spigot/`** - Complete Maven project

## 🚀 Features

### ✅ Core Algorithm (Preserved from Original)
- **Tree-based room generation** with Dijkstra pathfinding
- **Multi-room shapes**: 1x1, 1x2, 1x3, 1x4, L-shape, 2x2
- **Special room types**: Entrance (Green), Fairy, Blood (Boss), Trap, Puzzle
- **Door alignment system** with horizontal/vertical rotations
- **Critical path marking**: Green → Fairy → Blood routes
- **Bridge connections** for multi-block rooms
- **Deterministic generation** with seed support

### ✅ Enhanced Features (New)
- **External schematic loading** from plugin folders
- **World generation** - builds actual dungeons in-game
- **Async building** to prevent server lag
- **Hot reload** of schematic files
- **Auto folder structure** creation
- **Command system** for testing and generation

## 🎮 Usage

### Commands
- `/dungeon test` - Display ASCII dungeon map
- `/dungeon build [seed]` - Build actual dungeon at your location
- `/dungeon generate [seed]` - Test algorithm only
- `/dungeon reload` - Reload schematic files

### Installation
1. Build: `cd dungeon-generation-core-spigot && mvn clean package`
2. Copy: `target/dungeon-generation-core-1.0.0.jar` to `plugins/`
3. Start server (auto-creates schematic folders)
4. Add your schematics to `/plugins/DungeonGenerationCore/rooms/`

## 📂 Schematic System

Automatically creates organized folders:
```
plugins/DungeonGenerationCore/rooms/
├── 1x1/          # Standard rooms
├── 1x2/          # Rectangular rooms
├── 1x3/          # Long rooms  
├── 1x4/          # Very long rooms
├── L-shape/      # Corner rooms
├── 2x2/          # Large rooms
├── entrance/     # Green entrance rooms
├── blood/        # Red boss rooms
├── fairy/        # Purple miniboss rooms
├── trap/         # Trap rooms
└── puzzle/       # Puzzle rooms
```

**Supported formats**: JSON, .json.gz, .schematic (WorldEdit)

## 🔧 Technical Details

### Project Structure
- **Java 8** compatibility
- **Maven** build system
- **Spigot 1.8.8** API
- **15 Java classes** with complete functionality
- **Full documentation** and examples

### Performance
- **Async building**: 2 ticks per room
- **Memory efficient**: Cleanup after generation
- **Progress tracking**: Console logging
- **Error handling**: Production-ready

## 🎯 Algorithm Highlights

Your original sophisticated algorithm preserved with enhancements:

1. **Room Placement**: Entrance (left) → Fairy (center) → Blood (right)
2. **Tree Exploration**: BFS from Fairy room as root
3. **Dijkstra Pathfinding**: Optimal routes with door marking
4. **Shape Variety**: Complex multi-block room support
5. **Rotation System**: NW, NE, SE, SW orientations
6. **Door Types**: Normal, Bridge, Start, Wither, Fairy doors

## 🏗️ Ready for Production

This is your **complete dungeon generation system** ready for:
- ✅ Production Minecraft servers
- ✅ Custom schematic integration  
- ✅ Large-scale dungeon creation
- ✅ Integration with existing plugins

**Your Hypixel Skyblock Catacombs algorithm is now available as a standalone Spigot plugin!** 🎮