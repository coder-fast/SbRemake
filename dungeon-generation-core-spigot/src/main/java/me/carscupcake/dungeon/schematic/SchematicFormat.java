package me.carscupcake.dungeon.schematic;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Represents the data structure for a schematic file
 * Compatible with Minecraft 1.8.8 block system
 */
public class SchematicFormat {
    
    /**
     * Represents a single block in the schematic
     */
    public static class SchematicBlock {
        private final Material material;
        private final byte data; // Block data for 1.8.8
        private final int x, y, z;
        
        public SchematicBlock(Material material, byte data, int x, int y, int z) {
            this.material = material;
            this.data = data;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public byte getData() {
            return data;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getZ() {
            return z;
        }
        
        /**
         * Apply this block to the world
         */
        @SuppressWarnings("deprecation")
        public void applyToWorld(Block worldBlock) {
            worldBlock.setType(material);
            if (data != 0) {
                worldBlock.setData(data);
            }
        }
    }
    
    /**
     * Schematic metadata
     */
    public static class SchematicMeta {
        private final int width, height, length;
        private final String name;
        private final String originRotation;
        
        public SchematicMeta(int width, int height, int length, String name, String originRotation) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.name = name;
            this.originRotation = originRotation != null ? originRotation : "northwest";
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getLength() {
            return length;
        }
        
        public String getName() {
            return name;
        }
        
        public String getOriginRotation() {
            return originRotation;
        }
    }
}