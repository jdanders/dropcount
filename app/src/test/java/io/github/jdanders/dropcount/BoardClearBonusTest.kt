package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BoardClearBonusTest {

    private lateinit var gameEngine: GameEngine
    private lateinit var discGenerator: DiscGenerator

    @Before
    fun setup() {
        discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }

    @Test
    fun testBoardClearBonusDetection() {
        // Setup: A board with only one disc that will be matched by a drop
        var gameState = GameState(
            grid = List(GameState.GRID_SIZE) { r ->
                List(GameState.GRID_SIZE) { c ->
                    if (r == GameState.GRID_SIZE - 1 && c == 3) {
                        Cell.Occupied(Disc.Numbered(1)) // This will break when another 1 is dropped in col 3 or row becomes 1
                    } else {
                        Cell.Empty
                    }
                }
            },
            mode = GameMode.Normal,
            nextDisc = Disc.Numbered(1),
            dropsUntilNewRow = 10,
            status = GameStatus.Playing
        )

        // Sanity check: board is NOT empty
        assertFalse(gameState.grid.all { row -> row.all { it.isEmpty() } })

        // Action: Drop the disc that will cause a match and clear the board
        val result = gameEngine.dropDiscWithSteps(gameState, 3)

        // Verification
        assertTrue("Board clear should be detected", result.isBoardCleared)
        assertTrue("Board should be empty in final state", result.finalState.grid.all { row -> row.all { it.isEmpty() } })
        
        // Final score should be initial score (0) + match score (7) + board clear bonus (70000)
        // Note: each disc in match awards ScoringFormula.calculateScoreGain(1) = 7 points
        // There were two discs of value 1 in the column match? 
        // Wait, the dropped disc lands on top of the existing 1. Column count becomes 2. 
        // Value 1 doesn't match count 2.
        // Let's use value 2.
    }

    @Test
    fun testBoardClearBonusWithColumnMatch() {
        // Setup: One disc of value 2 in col 3, row 6.
        // Drop another disc of value 2 in col 3. It lands at row 5.
        // Column 3 now has 2 discs. Both are value 2. Both match count 2.
        // They both break. Board becomes empty.
        
        val initialScore = 1000
        var gameState = GameState(
            grid = List(GameState.GRID_SIZE) { r ->
                List(GameState.GRID_SIZE) { c ->
                    if (r == GameState.GRID_SIZE - 1 && c == 3) {
                        Cell.Occupied(Disc.Numbered(2))
                    } else {
                        Cell.Empty
                    }
                }
            },
            mode = GameMode.Normal,
            nextDisc = Disc.Numbered(2),
            dropsUntilNewRow = 10,
            score = initialScore,
            status = GameStatus.Playing
        )

        val result = gameEngine.dropDiscWithSteps(gameState, 3)

        assertTrue("Board clear should be detected", result.isBoardCleared)
        assertTrue("Board should be empty", result.finalState.grid.all { row -> row.all { it.isEmpty() } })
        
        val expectedScore = initialScore + (7 * 2) + GameConfig.BOARD_CLEAR_BONUS_POINTS
        assertEquals("Score should include 70k bonus", expectedScore, result.finalState.score)
    }
    
    @Test
    fun testBoardClearNotAwardedOnGameOver() {
        // Setup a full board except for one cell
        // Drop a disc in that cell to clear it, but also trigger a new row that causes game over
        // Actually, simpler: drop into a full column (handled in engine as game over)
        
        var gameState = GameState(
            grid = List(GameState.GRID_SIZE) { r ->
                List(GameState.GRID_SIZE) { c ->
                    if (c == 0) Cell.Occupied(Disc.Numbered(7)) else Cell.Empty
                }
            },
            mode = GameMode.Normal,
            nextDisc = Disc.Numbered(1),
            status = GameStatus.Playing
        )
        
        // Drop into full column 0
        val result = gameEngine.dropDiscWithSteps(gameState, 0)
        
        assertEquals(GameStatus.GameOver, result.finalState.status)
        assertFalse("Board clear bonus should not be awarded on GameOver", result.isBoardCleared)
    }
}
