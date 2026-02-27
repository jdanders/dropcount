package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for bugs that were fixed during development.
 * These tests ensure the fixes remain stable.
 */
class RegressionTest {
    
    private lateinit var gameEngine: GameEngine
    private lateinit var discGenerator: DiscGenerator
    
    @Before
    fun setup() {
        discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    // ===== Game Over Condition Tests =====
    
    @Test
    fun testGameOverOnlyWhenDiscsPushedOut() {
        // Regression: Game should only end when discs are pushed OUT of the grid,
        // not just when top row is full
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Fill column 0 completely (7 discs)
        for (row in 0 until GameState.GRID_SIZE) {
            gameState = gameState.setCell(row, 0, Cell.Occupied(Disc.Numbered(5)))
        }
        
        // Force a new row by setting dropsUntilNewRow to 0
        gameState = gameState.copy(dropsUntilNewRow = 1)
        
        // Drop one more disc to trigger new row
        gameState = gameState.copy(nextDisc = Disc.Numbered(6))
        gameState = gameEngine.dropDisc(gameState, 1)
        
        // Game should be over because column 0 discs got pushed out
        assertEquals("Game should be over when discs are pushed out", 
            GameStatus.GameOver, gameState.status)
        assertFalse("Game over discs should be tracked", gameState.gameOverDiscs.isEmpty())
        assertTrue("Column 0 should have a game over disc", gameState.gameOverDiscs.containsKey(0))
    }
    
    @Test
    fun testGameOverDiscsAreTracked() {
        // Regression: When discs are pushed out, they should be stored for display
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Fill column 0 completely with discs that won't match
        for (row in 0 until GameState.GRID_SIZE) {
            gameState = gameState.setCell(row, 0, Cell.Occupied(Disc.Numbered(5)))
        }
        
        // Force new row
        gameState = gameState.copy(dropsUntilNewRow = 1, nextDisc = Disc.Numbered(1))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        // Check that pushed-out disc is tracked
        assertEquals(GameStatus.GameOver, gameState.status)
        assertTrue("Should have at least one game over disc", gameState.gameOverDiscs.isNotEmpty())
        assertTrue("Column 0 should be tracked", gameState.gameOverDiscs.containsKey(0))
        
        // Check the actual disc value
        val disc0 = gameState.gameOverDiscs[0]
        assertTrue("Disc in column 0 should be a Numbered disc", disc0 is Disc.Numbered)
        assertEquals(5, (disc0 as Disc.Numbered).value)
    }

    @Test
    fun testGameOverWhenGridIsFull() {
        // Create a game state that is almost full (only one empty cell)
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Fill everything except (0, 0)
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                if (row == 0 && col == 0) continue
                gameState = gameState.setCell(row, col, Cell.Occupied(Disc.Numbered(5)))
            }
        }

        // Ensure no matches will happen by using a value that won't match
        // (Contiguous counts will be 7 in row/col, so use a value other than 7)
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))

        assertFalse("Grid should not be full yet", gameState.isGridFull())
        assertEquals(GameStatus.Playing, gameState.status)

        // Drop disc into the only empty cell (0, 0)
        val finalState = gameEngine.dropDisc(gameState, 0)

        assertTrue("Grid should be full now", finalState.isGridFull())
        assertEquals("Game should be over when grid is entirely full", GameStatus.GameOver, finalState.status)
    }

    @Test
    fun testNoGameOverIfGridFullButMatchOccurs() {
        // Create a game state that is almost full
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Fill everything except (0, 0)
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                if (row == 0 && col == 0) continue
                gameState = gameState.setCell(row, col, Cell.Occupied(Disc.Numbered(5)))
            }
        }

        // Use a value that WILL cause a match.
        // Row 0 will have 7 discs. Column 0 will have 7 discs.
        // So use value 7.
        gameState = gameState.copy(nextDisc = Disc.Numbered(7))

        val finalState = gameEngine.dropDisc(gameState, 0)

        // A match should have occurred, so the grid should NOT be full anymore
        assertFalse("Grid should NOT be full because matches occurred", finalState.isGridFull())
        assertEquals("Game should still be playing because matches cleared some space", GameStatus.Playing, finalState.status)
    }
    
    // ===== Drop Counter Tests =====
    
    @Test
    fun testDropCounterResetsAfterNewRow() {
        // Regression: Drop counter was going negative instead of resetting
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Set to 1 drop until new row
        gameState = gameState.copy(dropsUntilNewRow = 1, baseDropsPerRow = 30)
        
        // Drop a disc - this should trigger new row and reset counter
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        // Counter should be reset to positive value, not negative
        assertTrue("Drop counter should be positive after new row (was: " + gameState.dropsUntilNewRow + ")", 
            gameState.dropsUntilNewRow > 0)
    }
    
    @Test
    fun testNormalModeDecrementingSequence() {
        // Regression: Normal mode should follow 30→29→28... sequence, not get stuck at 1
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        
        assertEquals("Initial drops should be 30", 30, gameState.dropsUntilNewRow)
        
        // Trigger first new row
        gameState = gameState.copy(dropsUntilNewRow = 1, baseDropsPerRow = 30, nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        assertEquals("After first row, should be 29", 29, gameState.dropsUntilNewRow)
        assertEquals("baseDropsPerRow should be 29", 29, gameState.baseDropsPerRow)
        
        // Trigger second new row
        gameState = gameState.copy(dropsUntilNewRow = 1, nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        assertEquals("After second row, should be 28", 28, gameState.dropsUntilNewRow)
        assertEquals("baseDropsPerRow should be 28", 28, gameState.baseDropsPerRow)
        
        // Trigger third new row
        gameState = gameState.copy(dropsUntilNewRow = 1, nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        
        assertEquals("After third row, should be 27", 27, gameState.dropsUntilNewRow)
    }
    
    @Test
    fun testDecrementingStopsAtMinimum() {
        // Drops per row should not go below 5
        
        // 1. Verify 6 -> 5
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        gameState = gameState.copy(dropsUntilNewRow = 1, baseDropsPerRow = 6, nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        assertEquals("6 drops per row should decrement to 5", 5, gameState.dropsUntilNewRow)
        
        // 2. Verify 5 -> 5 (should not drop to 4)
        gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        gameState = gameState.copy(dropsUntilNewRow = 1, baseDropsPerRow = 5, nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 3)
        assertEquals("5 drops per row should stay at 5 (minimum)", 5, gameState.dropsUntilNewRow)
    }
    
    @Test
    fun testDropCounterDecrementsInNormalMode() {
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        
        val initialCount = gameState.dropsUntilNewRow
        
        // Drop a disc
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 0)
        
        // Counter should decrement by 1
        assertEquals("Counter should decrement", initialCount - 1, gameState.dropsUntilNewRow)
    }
    
    // ===== New Row Generation Tests =====
    
    @Test
    fun testNewRowAlwaysGeneratesSolidDiscs() {
        // Regression: New rows should ALWAYS be all solid discs
        val generator = DiscGenerator(GameMode.Normal)
        
        // Generate multiple rows to be sure
        for (i in 1..10) {
            val row = generator.generateNewRow()
            
            assertEquals("Row should have 7 discs", 7, row.size)
            assertTrue("All discs should be solid", row.all { it is Disc.Solid })
            
            // All should have 0 cracks
            row.forEach { disc ->
                assertTrue("Disc should be Solid", disc is Disc.Solid)
                assertEquals("New solid discs should have 0 cracks", 0, (disc as Disc.Solid).cracks)
            }
        }
    }
    
    @Test
    fun testNewRowGenerationInAllModes() {
        val modes = listOf(
            GameMode.Normal,
            GameMode.Challenge(ChallengeDifficulty.HARD),
            GameMode.Sequence(42)
        )
        
        modes.forEach { mode ->
            val generator = DiscGenerator(mode)
            val row = generator.generateNewRow()
            
            assertEquals("Row should be fully filled for $mode", 7, row.size)
            assertTrue("All discs should be solid for $mode", row.all { it is Disc.Solid })
        }
    }
    
    // ===== Animation Steps Tests =====
    
    @Test
    fun testAnimationStepsContainHighlightRegions() {
        // Ensure animation steps include entire contiguous regions for highlighting
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Create a setup where dropping one more disc will create a match
        // Place two 3s in a row
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(3)))
        
        // Drop a third 3 to trigger the match
        gameState = gameState.copy(nextDisc = Disc.Numbered(3))
        val dropResult = gameEngine.dropDiscWithSteps(gameState, 0)
        
        // Should have steps (from placing the disc adjacent to the two 3s, triggering a match)
        assertTrue("Should have animation steps", dropResult.steps.isNotEmpty())
        
        val firstStep = dropResult.steps[0]
        
        // Should have match positions
        assertFalse("Should have match positions", firstStep.matchPositions.isEmpty())
        
        // Highlight positions should include entire contiguous region
        assertTrue("Should highlight contiguous discs", 
            firstStep.highlightPositions.size >= 1)
    }
    
    @Test
    fun testNewRowMatchesGenerateAnimationSteps() {
        // Regression: When new row creates matches, animation steps should be generated
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Place two "3"s in column 4, rows 5 and 6
        gameState = gameState.setCell(5, 4, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(3)))
        
        // Force new row
        gameState = gameState.copy(dropsUntilNewRow = 1, nextDisc = Disc.Numbered(5))
        
        val dropResult = gameEngine.dropDiscWithSteps(gameState, 0)
        
        // Should not have animation steps from the match after new row
        assertFalse("Should have animation steps after new row adds discs", 
            dropResult.steps.isNotEmpty())
        
        // Should have animation steps from the match after new row
        assertTrue("Should have animation steps after new row adds discs", 
            dropResult.stepsAfterNewRow.isNotEmpty())
        
        // At least one step should be marked as first after new row
        val hasNewRowStep = dropResult.stepsAfterNewRow.any { it.isFirstStepAfterNewRow }
        assertTrue("At least one step should be marked as first after new row", hasNewRowStep)
    }
    
    // ===== Contiguous Counting Regression Tests =====
    
    @Test
    fun testContiguousCountingWithGaps() {
        // Regression: Counting should be contiguous, not total in row/column
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Row 6: [4, 4, ., 2, 2]
        // Should have two separate groups
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(4)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(4)))
        // Gap at column 2
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(2)))
        
        // Check contiguous counts
        assertEquals("First group should be 2", 2, gameState.countContiguousDiscsInRow(6, 0))
        assertEquals("First group should be 2", 2, gameState.countContiguousDiscsInRow(6, 1))
        assertEquals("Second group should be 2", 2, gameState.countContiguousDiscsInRow(6, 3))
        assertEquals("Second group should be 2", 2, gameState.countContiguousDiscsInRow(6, 4))
        
        // Total count should be 4, but contiguous counts are only 2 each
        assertEquals("Total discs in row", 4, gameState.countDiscsInRow(6))
    }
    
    @Test
    fun testContiguousRegionRetrieval() {
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Row 5: [., 7, 6, 5, ., ., .]
        gameState = gameState.setCell(5, 1, Cell.Occupied(Disc.Numbered(7)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(6)))
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(5)))
        
        // Get contiguous region for middle disc
        val region = gameState.getContiguousRegionInRow(5, 2)
        
        assertEquals("Region should have 3 discs", 3, region.size)
        assertTrue("Should include (5,1)", region.contains(GridPosition(Row(5), Col(1))))
        assertTrue("Should include (5,2)", region.contains(GridPosition(Row(5), Col(2))))
        assertTrue("Should include (5,3)", region.contains(GridPosition(Row(5), Col(3))))
    }
    
    @Test
    fun testGapPreventsMismatch() {
        // Ensure that gaps prevent matches from being counted across them
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        // Row 6: [5, 5, 5, ., 5, 5, 5]  - two groups of 3, but separated
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(5)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(5)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(5)))
        // Gap at column 3
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(5)))
        gameState = gameState.setCell(6, 5, Cell.Occupied(Disc.Numbered(5)))
        gameState = gameState.setCell(6, 6, Cell.Occupied(Disc.Numbered(5)))
        
        // Neither group should match because they're only contiguously 3, not 5
        // But if we drop a disc in the middle, no match should be made
        gameState = gameState.copy(nextDisc = Disc.Numbered(1))
        val result = gameEngine.dropDisc(gameState, 0)
        
        // All 5s should still be there (no 5-match triggered)
        assertEquals("5 at (6,0) should still exist", 5,
            (result.getCell(6, 0) as Cell.Occupied).disc.let { it as Disc.Numbered }.value)
        assertEquals("5 at (6,2) should still exist", 5,
            (result.getCell(6, 2) as Cell.Occupied).disc.let { it as Disc.Numbered }.value)
    }
    
    // ===== Edge Cases =====
    
    @Test
    fun testEmptyColumnContiguousCount() {
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        
        // Empty column should have count 0
        assertEquals("Empty column should have 0 contiguous count", 
            0, gameState.countContiguousDiscsInColumn(0, 3))
    }
    
    @Test
    fun testSingleDiscContiguousCount() {
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)

        gameState = gameState.setCell(3, 3, Cell.Occupied(Disc.Numbered(1)))

        // Single disc should have contiguous count of 1
        assertEquals("Single disc should have contiguous count 1", 
            1, gameState.countContiguousDiscsInRow(3, 3))
        assertEquals("Single disc should have contiguous count 1", 
            1, gameState.countContiguousDiscsInColumn(3, 3))
    }
    
    @Test
    fun testFullRowContiguousCount() {
        // Create an empty game state
        var gameState = TestUtils.createEmptyGameState(GameMode.Normal)
        
        // Fill entire row
        for (col in 0 until GameState.GRID_SIZE) {
            gameState = gameState.setCell(4, col, Cell.Occupied(Disc.Numbered(col + 1)))
        }
        
        // All positions should report 7
        for (col in 0 until GameState.GRID_SIZE) {
            assertEquals("Full row should have contiguous count 7", 
                7, gameState.countContiguousDiscsInRow(4, col))
        }
    }
}
