package io.github.jdanders.dropseven.model

/**
 * Represents a cell in the game grid. Can be empty or contain a disc.
 */
sealed class Cell {
    /**
     * An empty cell.
     */
    data object Empty : Cell()
    
    /**
     * A cell containing a disc.
     */
    data class Occupied(val disc: Disc) : Cell()
    
    /**
     * Returns true if this cell is empty.
     */
    fun isEmpty(): Boolean = this is Empty
    
    /**
     * Returns true if this cell contains a disc.
     */
    fun isOccupied(): Boolean = this is Occupied
    
    /**
     * Gets the disc if this cell is occupied, or null otherwise.
     */
    fun discOrNull(): Disc? = (this as? Occupied)?.disc
}

