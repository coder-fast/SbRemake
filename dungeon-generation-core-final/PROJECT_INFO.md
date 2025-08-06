# Dungeon Generation Core - Project Information

## 🏗️ Complete Maven Project Structure

This is a complete, ready-to-build Maven project for the Hypixel-style dungeon generation plugin.

## 📁 Project Structure

```
dungeon-generation-core/
├── pom.xml                           # Maven build configuration
├── .gitignore                        # Git ignore file
├── README.md                         # Comprehensive documentation
├── PROJECT_INFO.md                   # This file
├── example-1x1-room.json            # Example schematic file
└── src/
    ├── main/
    │   ├── java/
    │   │   └── me/carscupcake/dungeon/
    │   │       ├── DungeonGenerationCore.java      # Main plugin class
    │   │       ├── commands/
    │   │       │   └── DungeonCommand.java         # Command handler
    │   │       ├── generator/
    │   │       │   └── DungeonGenerator.java       # Core algorithm
    │   │       ├── room/
    │   │       │   ├── Room.java                   # Room data structure
    │   │       │   ├── RoomType.java               # Room types enum
    │   │       │   ├── RoomShape.java              # Room shapes & placement
    │   │       │   ├── DoorType.java               # Door types enum
    │   │       │   └── Rotation.java               # Rotation system
    │   │       ├── schematic/
    │   │       │   ├── SchematicFormat.java        # Schematic data structures
    │   │       │   └── SchematicLoader.java        # File loading system
    │   │       ├── util/
    │   │       │   └── Pos2d.java                  # 2D position utility
    │   │       └── world/
    │   │           └── DungeonWorldBuilder.java    # World generation
    │   └── resources/
    │       └── plugin.yml                          # Spigot plugin config
    └── test/
        └── java/                                   # (empty - ready for tests)
```

## 🚀 Build Instructions

### Prerequisites
- **Java 8** or higher
- **Maven 3.6+**
- **Git** (optional)

### Quick Build
```bash
# Clone or extract the project
cd dungeon-generation-core

# Build the plugin
mvn clean package

# The compiled JAR will be in target/
ls target/dungeon-generation-core-1.0.0.jar
```

### Development Build
```bash
# Clean build with tests
mvn clean compile test package

# Install to local repository
mvn clean install
```

## 📦 Installation

1. **Build the plugin** (see above)
2. **Copy JAR** to your server: `plugins/dungeon-generation-core-1.0.0.jar`
3. **Start server** - plugin will auto-create folder structure
4. **Add schematics** to `/plugins/DungeonGenerationCore/rooms/`
5. **Test generation** with `/dungeon test`
6. **Build dungeons** with `/dungeon build`

## 🎮 Usage Commands

- `/dungeon test` - Generate and display ASCII dungeon map
- `/dungeon generate [seed]` - Test algorithm with optional seed
- `/dungeon build [seed]` - Build actual dungeon in world
- `/dungeon reload` - Reload schematic files
- `/dg` - Alias for all commands

## 📂 Schematic Folder Structure

The plugin automatically creates:
```
plugins/DungeonGenerationCore/rooms/
├── 1x1/          # 1x1 room schematics
├── 1x2/          # 1x2 room schematics  
├── 1x3/          # 1x3 room schematics
├── 1x4/          # 1x4 room schematics
├── L-shape/      # L-shaped room schematics
├── 2x2/          # 2x2 room schematics
├── entrance/     # Entrance (green) room schematics
├── blood/        # Blood (boss) room schematics
├── fairy/        # Fairy room schematics
├── trap/         # Trap room schematics
└── puzzle/       # Puzzle room schematics
```

## 🔧 Technical Features

### ✅ Complete Implementation
- **Tree-based room generation** with Dijkstra pathfinding
- **Multi-room shapes** (1x1, 1x2, 1x3, 1x4, L-shape, 2x2)
- **External schematic loading** (JSON, .gz, .schematic)
- **World generation** with block placement
- **Door alignment** system (horizontal/vertical)
- **Room rotation** handling (NW, NE, SE, SW)
- **Async building** to prevent server lag

### 🎯 Algorithm Highlights
- **Green → Fairy → Blood** pathfinding with Dijkstra
- **Tree exploration** starting from Fairy room
- **Bridge connections** for multi-block rooms
- **Critical path marking** with special door types
- **Deterministic generation** with seed support

## 🔨 Development

### Adding New Features
1. **Room Types**: Edit `RoomType.java` enum
2. **Room Shapes**: Add to `RoomShape.java` with placement logic
3. **Door Types**: Extend `DoorType.java` and update world builder
4. **Commands**: Add subcommands to `DungeonCommand.java`

### Schematic Support
- **JSON Format**: Primary format (like original implementation)
- **Gzipped JSON**: For smaller file sizes
- **WorldEdit .schematic**: Basic support (extensible)

### Performance
- **Async building**: 2 ticks per room
- **Progress tracking**: Console logging
- **Memory efficient**: Cleanup after generation

## 📋 Next Steps

1. **Add your schematics** to the rooms folders
2. **Test the generation** with `/dungeon test`
3. **Build test dungeons** with `/dungeon build`
4. **Customize** room types or add new features
5. **Integrate** with your existing systems

## 🐛 Troubleshooting

### Build Issues
```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Force update dependencies
mvn clean install -U
```

### Plugin Issues
- Check console for startup errors
- Verify Java 8 compatibility
- Ensure Spigot 1.8.8 server
- Check `/plugins/DungeonGenerationCore/` folder permissions

## 📞 Support

This plugin is based on your original Minestom implementation with full algorithm preservation. All core features have been ported to Spigot 1.8.8 with enhanced schematic and world generation capabilities.

**Ready for production use!** 🏰