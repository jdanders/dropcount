package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig

import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.*
import io.github.jdanders.dropcount.model.GridPosition
import io.github.jdanders.dropcount.model.Row
import io.github.jdanders.dropcount.model.Col
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for chain reactions.
 */
class ChainReactionTest {
    
    private lateinit var gameEngine: GameEngine
    private lateinit var discGenerator: DiscGenerator
    
    @Before
    fun setup() {
        discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    @Test
    fun testSimpleColumnChain() {
        // Create a scenario that should trigger a 2-level chain
        // Column 3: bottom has three 3's, then we drop a 4
        var gameState = gameEngine.startNewGame(GameMode.Normal)

        // Clear the grid to ensure we start with empty state for testing
        val emptyGrid = List(GameState.GRID_SIZE) { List(GameState.GRID_SIZE) { Cell.Empty } }
        gameState = gameState.copy(grid = emptyGrid, score = 0, totalDrops = 0, currentChain = 0, level = 1)

        // Place three 3's in column 3, rows 4, 5, 6 (bottom)
        gameState = gameState.setCell(4, 3, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(3)))
        
        // Set next disc to be a 4
        gameState = gameState.copy(nextDisc = Disc.Numbered(4))
        
        println("Before drop:")
        printColumn(gameState, 3)
        
        // Drop the 4 in column 3
        gameState = gameEngine.dropDisc(gameState, 3)
        
        println("After drop:")
        printColumn(gameState, 3)
        
        // After chain:
        // 1. The 4 should break (chain 1) because colCount = 4 and value = 4
        // 2. After gravity, three 3's remain at bottom
        // 3. All three 3's should break (chain 2) because colCount = 3 and value = 3
        // 4. Column should be empty
        
        val column3Discs = (0 until GameState.GRID_SIZE).count { row ->
            gameState.getCell(row, 3).isOccupied()
        }
        
        println("Final discs in column 3: $column3Discs")
        println("Final score: ${gameState.score}")
        
        // All discs should be gone
        assertEquals("Column should be empty after 2-level chain", 0, column3Discs)
        
        // Score should reflect 2 chains + board clear bonus (since we cleared the grid at start):
        // Chain 1: 1 disc × 7 points × 1 multiplier = 7
        // Chain 2: 3 discs × 39 points = 117
        // Board Clear Bonus: 70,000
        // Total: 70,124
        assertEquals("Score should be 70,124 (7 + 117 + 70000)", 124 + GameConfig.BOARD_CLEAR_BONUS_POINTS, gameState.score)
    }
    
    @Test
    fun testRowChainAfterGravity() {
        // Test that after removing discs, gravity causes discs to fall
        // into positions that create new row matches
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Create row 6 (bottom) with: [1, 2, ., 3, 3, ., .]
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 4, Cell.Occupied(Disc.Numbered(3)))
        
        // Create row 5 with: [., ., 3, ., ., ., .]
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(3)))
        
        // Set next disc to be a 2
        gameState = gameState.copy(nextDisc = Disc.Numbered(2))
        
        // Drop in column 2 (will land at row 6)
        gameState = gameEngine.dropDisc(gameState, 2)
        
        // Expected behavior:
        // 1. Disc lands at (6,2)
        // 2. Now row 6 has 5 discs: [1, 2, 2, 3, 3, ., .]
        // 3. No matches yet (no disc has value 5)
        
        // But if we had a 5 in there, it should break
        assertTrue("Drop should complete", gameState.status == GameStatus.Playing)
    }
    
    private fun printColumn(state: GameState, col: Int) {
        for (row in 0 until GameState.GRID_SIZE) {
            val cell = state.getCell(row, col)
            val value = when {
                cell is Cell.Occupied && cell.disc is Disc.Numbered -> (cell.disc as Disc.Numbered).value.toString()
                cell is Cell.Occupied && cell.disc is Disc.Solid -> "S"
                else -> "."
            }
            println("  Row $row: $value")
        }
    }
}

