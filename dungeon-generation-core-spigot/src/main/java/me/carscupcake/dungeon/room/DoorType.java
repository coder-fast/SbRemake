package me.carscupcake.dungeon.room;

/**
 * Types of doors/connections between rooms
 */
public enum DoorType {
    /**
     * No door/connection
     */
    NONE,
    
    /**
     * Bridge connections for multi-block rooms
     */
    BRIDGE,
    
    /**
     * Starting door (green)
     */
    START,
    
    /**
     * Normal door
     */
    NORMAL,
    
    /**
     * Wither door (critical path marker)
     */
    WITHER,
    
    /**
     * Fairy door (path to fairy room)
     */
    FAIRY,
    
    /**
     * Blood door (path to blood room)
     */
    BLOOD
}