# Dungeon Generation Core

A Hypixel Skyblock Catacombs-style dungeon generation plugin for Minecraft 1.8.8 using Spigot API.

## Features

### ✅ Implemented
- **Tree-based Room Generation** - Rooms arranged in tree structures with different shapes
- **Multiple Room Shapes**: 1x1, 1x2, 1x3, 1x4, L-shape, 2x2
- **Special Room Types**: Entrance (Green), Fairy, Blood (Boss), Trap, Puzzle, Regular rooms
- **Dijkstra Pathfinding** - Ensures optimal paths from Green → Fairy → Blood rooms
- **Door Alignment System** - Horizontal and vertical door rotations with proper connections
- **Room Rotation System** - NW, NE, SE, SW orientations for varied layouts
- **Bridge Connections** - Internal doors for multi-block rooms
- **Critical Path Marking** - Wither and Fairy doors mark the essential route
- **Deterministic Generation** - Same seed produces identical dungeons
- **Schematic System** - Load room templates from external files (JSON, .schematic, .gz)
- **World Generation** - Convert room data to actual blocks in the world
- **Automatic Folder Structure** - Creates organized folders for different room types

### 📋 Planned
- WorldEdit .schematic format support (NBT reading)
- Advanced schematic features (entity support, chest contents)
- Performance optimization for large dungeons
- Integration with existing dungeon plugins

## Algorithm Overview

The dungeon generation follows these key principles from your original Minestom implementation:

### 1. **Room Placement Algorithm**
```
1. Place Entrance (Green) on left edge
2. Place Blood room (Boss) on right edge  
3. Place Fairy room in center with empty spaces around it
4. Place Trap room adjacent to Fairy or Entrance
5. Place 2-4 Puzzle rooms with spacing requirements
6. Fill remaining spaces with various room shapes (tree format)
7. Fill empty spots with 1x1 rooms
```

### 2. **Tree Exploration for Room Discovery**
Starting from the Fairy room as the root node, the algorithm uses breadth-first traversal to discover and connect all reachable rooms, creating a tree structure for exploration.

### 3. **Dijkstra Pathfinding for Critical Routes**
Two essential paths are calculated:
- **Green → Fairy**: Primary progression route
- **Fairy → Blood**: Boss access route

The algorithm marks these paths with special door types (Wither/Fairy doors) to indicate the critical path.

### 4. **Door Alignment System**
- **Horizontal doors**: East-West connections between rooms
- **Vertical doors**: North-South connections between rooms  
- **Bridge doors**: Internal connections within multi-block rooms
- **Special doors**: Start, Wither, Fairy door types for critical paths

## Commands

- `/dungeon test` - Generate and display ASCII representation of dungeon layout
- `/dungeon generate [seed]` - Generate dungeon algorithm with optional seed
- `/dungeon build [seed]` - Build actual dungeon in world at your location
- `/dungeon reload` - Reload schematic files from disk
- `/dg` - Alias for `/dungeon`

## Permissions

- `dungeon.admin` - Access to all dungeon generation commands (default: op)

## Installation

1. Download the latest release JAR file
2. Place in your server's `plugins` folder  
3. Restart the server
4. Use `/dungeon test` to verify installation
5. Place your schematic files in `/plugins/DungeonGenerationCore/rooms/`

### Folder Structure
The plugin automatically creates this structure:
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

### Schematic Formats
Supported schematic file formats:

#### JSON Format (Recommended)
```json
{
  "name": "my-room",
  "originRotation": "northwest",
  "width": 32,
  "height": 16, 
  "length": 32,
  "blocks": [
    // 3D array: blocks[x][y][z] = blockId
    // Use 1.8.8 block IDs (Stone=1, Stone Bricks=98, Air=0)
  ]
}
```

#### Supported Files
- **`.json`** - JSON format (like your original implementation)
- **`.json.gz`** - Gzipped JSON for smaller file sizes
- **`.schematic`** - WorldEdit format (basic support)

#### Block Coordinates
- Each room is **32×32 blocks** (configurable height up to 32)
- Doors should be at room edges: center positions (15-16 on each side)
- Use air blocks (ID: 0) for door openings
- The plugin handles rotation automatically

## Building from Source

### Requirements
- Java 8
- Maven 3.6+

### Build Steps
```bash
git clone <repository>
cd dungeon-generation-core
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Configuration

Currently, all configuration is handled through command parameters. Future versions will include config files for:
- Default dungeon size (currently 6x6)
- Room shape probabilities
- Special room placement rules
- Pathfinding weights

## Technical Details

### Room Shape System
Each room shape defines:
- **Children positions** for multi-block rooms
- **Rotation offsets** for proper world placement
- **Insertion logic** to check placement validity

### Pathfinding Implementation
Uses Dijkstra's algorithm with:
- **Distance penalties** for non-regular rooms
- **Door type checking** for movement validation
- **Path reconstruction** with special door marking

### Coordinate System
- Grid coordinates (room positions): 0-5 for 6x6 dungeon
- World coordinates: Grid × 32 (each room = 32×32 blocks)
- Y-level: Fixed at 70 for testing

## Integration with Your Original Code

This plugin is directly based on your Minestom implementation with these adaptations:

### From Minestom → Spigot
- `Point/Vec` → `Location`
- `MinestomServer.getLogger()` → `Bukkit.getLogger()`
- `net.minestom` packages → `org.bukkit` packages
- Chunk loading system → Command-based testing
- Virtual threads → Standard threading

### Preserved Features
- Exact same generation algorithm
- All room shapes and rotations
- Door placement logic
- Dijkstra pathfinding
- Special room positioning rules

## Example Output

```
=== DUNGEON MAP ===
[E]|[R]|[R] [P] [R] [B]
---+---+-------+---+---
[R]|[T] [R] [R] [R] [R]
---+-------+---+---+---
[R] [R] [F] [R] [R] [R]
---+---+---+---+---+---
[R] [R] [R] [R] [R] [R]
---+---+---+---+---+---
[R] [R] [R] [R] [R] [R]
---+---+---+---+---+---
[R] [R] [R] [R] [R] [R]

Legend:
[E] = Entrance [F] = Fairy [B] = Blood [T] = Trap
[P] = Puzzle [R] = Room
```

## Future Development

This core provides the foundation for:
1. **World Builder** - Convert room data to actual blocks
2. **Schematic System** - Load room templates from files  
3. **Entity System** - Spawn mobs and NPCs
4. **Loot System** - Generate dungeon rewards
5. **Integration APIs** - Connect with other plugins

## Contributing

When contributing, please maintain:
- Java 8 compatibility
- Spigot 1.8.8 API compliance
- Algorithm consistency with original implementation
- Comprehensive documentation

## License

Based on the original Minestom implementation by CarsCupcake.
Adapted for Spigot 1.8.8 compatibility.