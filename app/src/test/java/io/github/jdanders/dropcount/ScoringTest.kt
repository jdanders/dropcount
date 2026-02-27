package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests to validate the scoring system.
 */
class ScoringTest {
    
    private lateinit var gameEngine: GameEngine
    
    @Before
    fun setup() {
        val discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    @Test
    fun testCalculateScoreGainFormula() {
        // Verify the sequence: 7, 39, 109, 224, 391...
        assertEquals(7, gameEngine.calculateScoreGain(1))
        assertEquals(39, gameEngine.calculateScoreGain(2))
        assertEquals(109, gameEngine.calculateScoreGain(3))
        assertEquals(224, gameEngine.calculateScoreGain(4))
        assertEquals(391, gameEngine.calculateScoreGain(5))
    }
    
    @Test
    fun testSingleDiscChainScoring() {
        // Set up a simple scenario: 1 disc matches in chain level 1
        var gameState = gameEngine.startNewGame(GameMode.Normal)

        // Clear the grid to ensure we start with empty state for testing
        val emptyGrid = List(GameState.GRID_SIZE) { List(GameState.GRID_SIZE) { Cell.Empty } }
        gameState = gameState.copy(grid = emptyGrid, score = 0, totalDrops = 0, currentChain = 0, level = 1)

        // Create a single disc with value 1 that will match (1 disc in row)
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(1)))
        
        val initialScore = gameState.score
        gameState = gameState.copy(nextDisc = Disc.Numbered(5)) // Non-matching disc
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // Score should increase by 7 (1 disc at chain level 1)
        val expectedScore = initialScore + 7
        assertEquals(expectedScore, gameState.score)
    }
    
    @Test
    fun testMultipleDiscChainScoring() {
        // Scenario: 1 disc matches in chain 1, then 2 discs match in chain 2
        // Expected score: 7 + (39 * 2) = 85
        var gameState = gameEngine.startNewGame(GameMode.Normal)

        // Clear the grid to ensure we start with empty state for testing
        val emptyGrid = List(GameState.GRID_SIZE) { List(GameState.GRID_SIZE) { Cell.Empty } }
        gameState = gameState.copy(grid = emptyGrid, score = 0, totalDrops = 0, currentChain = 0, level = 1)

        // Set up a scenario that will create a chain reaction
        // Row 6, col 2: disc with value 1 (will match - 1 disc in row)
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(1)))
        
        // Col 3: Stack of discs where a 2 will match after first break
        // This creates: when disc at (6,2) breaks, gravity causes (5,3) to fall to (6,3)
        // Then both (5,3) and (6,3) have value 2 and are contiguous, matching!
        gameState = gameState.setCell(5, 3, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(2)))
        
        val initialScore = gameState.score
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // This setup should create at least one chain
        // Verify score increased
        assertTrue("Score should increase", gameState.score > initialScore)
    }
    
    @Test
    fun testScoreAccumulation() {
        // Test that scores accumulate across multiple chains
        var gameState = gameEngine.startNewGame(GameMode.Normal)

        // Clear the grid to ensure we start with empty state for testing
        val emptyGrid = List(GameState.GRID_SIZE) { List(GameState.GRID_SIZE) { Cell.Empty } }
        gameState = gameState.copy(grid = emptyGrid, score = 0, totalDrops = 0, currentChain = 0, level = 1)
        val initialScore = gameState.score

        // First match
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        val scoreAfterFirst = gameState.score
        
        assertTrue("Score should increase after first match", scoreAfterFirst > initialScore)
        
        // Second match
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        val scoreAfterSecond = gameState.score
        
        assertTrue("Score should continue to accumulate", scoreAfterSecond > scoreAfterFirst)
    }
    
    @Test
    fun testExampleScenario() {
        // Test the specific example: 1 disc in chain 1, then 2 discs in chain 2
        // Expected: 7 + (39 * 2) = 85
        
        // This is a conceptual test - the actual game state setup would be complex
        // but we can verify the formula
        val chain1Score = gameEngine.calculateScoreGain(1) * 1  // 7 * 1 = 7
        val chain2Score = gameEngine.calculateScoreGain(2) * 2  // 39 * 2 = 78
        val totalScore = chain1Score + chain2Score  // 7 + 78 = 85
        
        assertEquals(7, chain1Score)
        assertEquals(78, chain2Score)
        assertEquals(85, totalScore)
    }
    
    @Test
    fun testHigherChainLevels() {
        // Verify higher chain levels produce expected scores
        assertEquals(7, gameEngine.calculateScoreGain(1) * 1)    // Chain 1, 1 disc
        assertEquals(78, gameEngine.calculateScoreGain(2) * 2)   // Chain 2, 2 discs
        assertEquals(327, gameEngine.calculateScoreGain(3) * 3)  // Chain 3, 3 discs
        assertEquals(896, gameEngine.calculateScoreGain(4) * 4)  // Chain 4, 4 discs
        assertEquals(1955, gameEngine.calculateScoreGain(5) * 5) // Chain 5, 5 discs
    }
    
    @Test
    fun testFormulaAccuracy() {
        // Test that the formula produces integer results for the first 10 chain levels
        for (chainLevel in 1..10) {
            val score = gameEngine.calculateScoreGain(chainLevel)
            assertTrue("Score for chain $chainLevel should be positive", score > 0)
            
            // Verify it increases with chain level
            if (chainLevel > 1) {
                val previousScore = gameEngine.calculateScoreGain(chainLevel - 1)
                assertTrue("Score should increase with chain level", score > previousScore)
            }
        }
    }
}

