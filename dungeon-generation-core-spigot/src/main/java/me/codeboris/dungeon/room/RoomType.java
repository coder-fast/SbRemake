package me.codeboris.dungeon.room;

/**
 * Different types of rooms in the dungeon
 */
public enum RoomType {
    /**
     * Green room - starting point
     */
    ENTRANCE,
    
    /**
     * Regular dungeon rooms with mobs
     */
    ROOM,
    
    /**
     * Blood room - final boss room
     */
    BLOOD,
    
    /**
     * Fairy room - special miniboss room
     */
    FAIRY,
    
    /**
     * Puzzle rooms
     */
    PUZZLE,
    
    /**
     * Trap rooms
     */
    TRAP,
    
    /**
     * Mini boss rooms
     */
    MINI
}