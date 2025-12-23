package io.github.jdanders.dropseven

import io.github.jdanders.dropseven.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for GameState class functionality.
 */
class GameStateTest {
    
    @Test
    fun testInitialGameState() {
        val state = GameState()
        
        assertEquals(0, state.score)
        assertEquals(0, state.totalDrops)
        assertEquals(GameStatus.Playing, state.status)
        assertEquals(0, state.currentChain)
        
        // Grid should be empty
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                assertTrue(state.getCell(row, col).isEmpty())
            }
        }
    }
    
    @Test
    fun testSetAndGetCell() {
        var state = GameState()
        val disc = Disc.Numbered(5)
        
        state = state.setCell(3, 4, Cell.Occupied(disc))
        
        val cell = state.getCell(3, 4)
        assertTrue(cell.isOccupied())
        assertEquals(disc, (cell as Cell.Occupied).disc)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testGetCellOutOfBoundsRow() {
        val state = GameState()
        state.getCell(7, 3)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testGetCellOutOfBoundsColumn() {
        val state = GameState()
        state.getCell(3, 7)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testSetCellOutOfBounds() {
        val state = GameState()
        state.setCell(7, 7, Cell.Empty)
    }
    
    @Test
    fun testIsColumnFull() {
        var state = GameState()
        
        // Fill column 2 completely
        for (row in 0 until GameState.GRID_SIZE) {
            state = state.setCell(row, 2, Cell.Occupied(Disc.Numbered(1)))
        }
        
        assertTrue(state.isColumnFull(2))
        assertFalse(state.isColumnFull(1))
        assertFalse(state.isColumnFull(3))
    }
    
    @Test
    fun testGetFirstEmptyRow() {
        var state = GameState()
        
        // Empty column should return bottom row
        assertEquals(6, state.getFirstEmptyRow(0))
        
        // Add discs from bottom
        state = state.setCell(6, 0, Cell.Occupied(Disc.Numbered(1)))
        assertEquals(5, state.getFirstEmptyRow(0))
        
        state = state.setCell(5, 0, Cell.Occupied(Disc.Numbered(2)))
        assertEquals(4, state.getFirstEmptyRow(0))
        
        // Fill the column
        for (row in 4 downTo 0) {
            state = state.setCell(row, 0, Cell.Occupied(Disc.Numbered(1)))
        }
        
        assertNull(state.getFirstEmptyRow(0))
    }
    
    @Test
    fun testCountDiscsInRow() {
        var state = GameState()
        
        assertEquals(0, state.countDiscsInRow(3))
        
        // Add 3 discs to row 3
        state = state.setCell(3, 1, Cell.Occupied(Disc.Numbered(1)))
        state = state.setCell(3, 3, Cell.Occupied(Disc.Numbered(2)))
        state = state.setCell(3, 5, Cell.Occupied(Disc.Numbered(3)))
        
        assertEquals(3, state.countDiscsInRow(3))
        assertEquals(0, state.countDiscsInRow(2))
    }
    
    @Test
    fun testCountDiscsInColumn() {
        var state = GameState()
        
        assertEquals(0, state.countDiscsInColumn(2))
        
        // Add 4 discs to column 2
        state = state.setCell(0, 2, Cell.Occupied(Disc.Numbered(1)))
        state = state.setCell(2, 2, Cell.Occupied(Disc.Numbered(2)))
        state = state.setCell(4, 2, Cell.Occupied(Disc.Numbered(3)))
        state = state.setCell(6, 2, Cell.Occupied(Disc.Numbered(4)))
        
        assertEquals(4, state.countDiscsInColumn(2))
        assertEquals(0, state.countDiscsInColumn(1))
    }
    
    @Test
    fun testGetRowPositions() {
        val state = GameState()
        val positions = state.getRowPositions(3)
        
        assertEquals(7, positions.size)
        assertEquals(3 to 0, positions[0])
        assertEquals(3 to 6, positions[6])
    }
    
    @Test
    fun testGetColumnPositions() {
        val state = GameState()
        val positions = state.getColumnPositions(4)
        
        assertEquals(7, positions.size)
        assertEquals(0 to 4, positions[0])
        assertEquals(6 to 4, positions[6])
    }
    
    @Test
    fun testGameStateImmutability() {
        val state1 = GameState()
        val state2 = state1.setCell(3, 3, Cell.Occupied(Disc.Numbered(5)))
        
        // Original state should be unchanged
        assertTrue(state1.getCell(3, 3).isEmpty())
        
        // New state should have the disc
        assertTrue(state2.getCell(3, 3).isOccupied())
    }
    
    @Test
    fun testMultipleCellUpdates() {
        var state = GameState()
        
        // Update multiple cells
        for (col in 0 until 5) {
            state = state.setCell(6, col, Cell.Occupied(Disc.Numbered(col + 1)))
        }
        
        // Verify all updates
        for (col in 0 until 5) {
            val cell = state.getCell(6, col)
            assertTrue(cell.isOccupied())
            assertEquals(col + 1, ((cell as Cell.Occupied).disc as Disc.Numbered).value)
        }
        
        // Verify other cells are still empty
        assertTrue(state.getCell(6, 5).isEmpty())
        assertTrue(state.getCell(5, 0).isEmpty())
    }
}

