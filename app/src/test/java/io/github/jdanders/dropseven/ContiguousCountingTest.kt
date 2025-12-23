package io.github.jdanders.dropseven

import io.github.jdanders.dropseven.engine.DiscGenerator
import io.github.jdanders.dropseven.engine.GameEngine
import io.github.jdanders.dropseven.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for contiguous (adjacent) disc counting logic.
 */
class ContiguousCountingTest {
    
    private lateinit var gameEngine: GameEngine
    
    @Before
    fun setup() {
        val discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    // ===== Contiguous Counting Tests =====
    
    @Test
    fun testContiguousRowCount() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Place: [., disc, disc, disc, ., disc, .]
        // Positions 1-3 form a contiguous group of 3
        gameState = gameState.setCell(5, 1, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(5, 5, Cell.Occupied(Disc.Numbered(1)))
        
        // Check contiguous count for middle disc
        assertEquals(3, gameState.countContiguousDiscsInRow(5, 2))
        
        // Check contiguous count for isolated disc
        assertEquals(1, gameState.countContiguousDiscsInRow(5, 5))
    }
    
    @Test
    fun testContiguousColumnCount() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Place discs in column 2: rows 4, 5, 6 (contiguous group of 3)
        gameState = gameState.setCell(4, 2, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(3)))
        
        // Place isolated disc at row 2 (with gap at row 3)
        gameState = gameState.setCell(2, 2, Cell.Occupied(Disc.Numbered(1)))
        
        // Check contiguous count for disc in group of 3
        assertEquals(3, gameState.countContiguousDiscsInColumn(5, 2))
        
        // Check contiguous count for isolated disc
        assertEquals(1, gameState.countContiguousDiscsInColumn(2, 2))
    }
    
    @Test
    fun testMatchWithGap() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Use columns instead of rows to avoid gap-filling issues
        // Column 0: two 2's at rows 5-6 (contiguous group of 2, will match)
        // Column 1: empty (gap)
        // Column 2: two 4's at rows 5-6 (contiguous group of 2, won't match since value is 4)
        gameState = gameState.setCell(5, 0, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(4)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(4)))
        
        // Drop a non-matching disc in column 3 to trigger processing (doesn't affect columns 0 or 2)
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        // The "2"s in column 0 should have matched and disappeared
        assertTrue("Disc at (5,0) should be empty after match", gameState.getCell(5, 0).isEmpty())
        assertTrue("Disc at (6,0) should be empty after match", gameState.getCell(6, 0).isEmpty())
        
        // The "4"s in column 2 should still be there (contiguous count = 2, but value is 4)
        assertTrue("Disc at (5,2) should still exist", gameState.getCell(5, 2).isOccupied())
        assertTrue("Disc at (6,2) should still exist", gameState.getCell(6, 2).isOccupied())
    }
    
    @Test
    fun testNoMatchWithGap() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Row 6: [4, 4, ., 4, 4]
        // Two separate groups of 2 discs each
        // Even though total count is 4, no disc should match because contiguous counts are only 2
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(4)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(4)))
        // Gap at position 2
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(4)))
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(4)))
        
        // Drop a disc elsewhere to trigger processing
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // All "4"s should still be there (contiguous count = 2, not 4)
        assertTrue("Disc at (6,0) should still exist", gameState.getCell(6, 0).isOccupied())
        assertTrue("Disc at (6,1) should still exist", gameState.getCell(6, 1).isOccupied())
        assertTrue("Disc at (6,3) should still exist", gameState.getCell(6, 3).isOccupied())
        assertTrue("Disc at (6,4) should still exist", gameState.getCell(6, 4).isOccupied())
    }
    
    @Test
    fun testMatchWithMixedTypes() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Row 6: [Solid, 2, 2]
        // Contiguous group of 3 discs, so the "2"s won't match (need exactly 2)
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Solid(0, 5)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(2)))
        
        // Drop a disc to trigger processing
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // The "2"s should still be there (contiguous count = 3, not 2)
        // because solid discs count toward the contiguous count
        assertTrue("Disc at (6,1) should still exist", gameState.getCell(6, 1).isOccupied())
        assertTrue("Disc at (6,2) should still exist", gameState.getCell(6, 2).isOccupied())
    }
    
    @Test
    fun testSingleDiscMatch() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Place a single "1" disc (contiguous count = 1, value = 1, should match)
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(1)))
        
        // Drop a disc elsewhere to trigger processing
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // The "1" should have disappeared
        assertTrue("Single 1 disc should match and disappear", gameState.getCell(6, 3).isEmpty())
    }
    
    @Test
    fun testGetContiguousRegionInRow() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Row 5: [., disc, disc, disc, ., disc, .]
        gameState = gameState.setCell(5, 1, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(5, 5, Cell.Occupied(Disc.Numbered(1)))
        
        // Get contiguous region for disc at (5, 2)
        val region = gameState.getContiguousRegionInRow(5, 2)
        
        assertEquals(3, region.size)
        assertTrue(region.contains(5 to 1))
        assertTrue(region.contains(5 to 2))
        assertTrue(region.contains(5 to 3))
        assertFalse(region.contains(5 to 5))
    }
    
    @Test
    fun testGetContiguousRegionInColumn() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Column 2: rows 4, 5, 6 (contiguous), gap, then row 2 (isolated)
        gameState = gameState.setCell(4, 2, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(3)))
        // Gap at row 3
        gameState = gameState.setCell(2, 2, Cell.Occupied(Disc.Numbered(1)))
        
        // Get contiguous region for disc at (5, 2)
        val region = gameState.getContiguousRegionInColumn(5, 2)
        
        assertEquals(3, region.size)
        assertTrue(region.contains(4 to 2))
        assertTrue(region.contains(5 to 2))
        assertTrue(region.contains(6 to 2))
        assertFalse(region.contains(2 to 2))
    }
    
    @Test
    fun testFullRowMatch() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Fill entire row 6 with 7 discs, one of which is a "7"
        for (col in 0 until GameState.GRID_SIZE) {
            val value = if (col == 3) 7 else (col % 6) + 1
            gameState = gameState.setCell(6, col, Cell.Occupied(Disc.Numbered(value)))
        }
        
        // Drop a disc elsewhere to trigger processing (in a different row)
        gameState = gameState.setCell(5, 5, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 0)
        
        // The "7" at position (6, 3) should have matched and disappeared
        assertTrue("The 7 disc should have matched (contiguous = 7)", gameState.getCell(6, 3).isEmpty())
    }
    
    @Test
    fun testMultipleMatchesInDifferentGroups() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Row 5: [1, ., 2, 2, ., ., .]
        // Row 6: [., ., ., ., 3, 3, 3]
        // Two rows with separate contiguous groups: one 1, two 2s on row 5; three 3s on row 6
        gameState = gameState.setCell(5, 0, Cell.Occupied(Disc.Numbered(1)))
        // Gap at 1
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(2)))
        
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 5, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 6, Cell.Occupied(Disc.Numbered(3)))
        
        // Drop a disc elsewhere to trigger processing
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 1) // Drop in column 1 (will be at row 6)
        
        // All the matched discs should have disappeared
        assertTrue("1 at (5,0) should match", gameState.getCell(5, 0).isEmpty())
        assertTrue("2 at (5,2) should match", gameState.getCell(5, 2).isEmpty())
        assertTrue("2 at (5,3) should match", gameState.getCell(5, 3).isEmpty())
        assertTrue("3 at (6,4) should match", gameState.getCell(6, 4).isEmpty())
        assertTrue("3 at (6,5) should match", gameState.getCell(6, 5).isEmpty())
        assertTrue("3 at (6,6) should match", gameState.getCell(6, 6).isEmpty())
    }
}

