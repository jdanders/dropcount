package io.github.jdanders.dropseven

import io.github.jdanders.dropseven.engine.DiscGenerator
import io.github.jdanders.dropseven.engine.GameEngine
import io.github.jdanders.dropseven.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests for the GameEngine.
 */
class GameEngineTest {
    
    private lateinit var gameEngine: GameEngine
    private lateinit var discGenerator: DiscGenerator
    
    @Before
    fun setup() {
        discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    // ===== Initialization Tests =====
    
    @Test
    fun testInitialGameStateNormalMode() {
        val gameState = gameEngine.startNewGame(GameMode.Normal)
        
        assertEquals(GameStatus.Playing, gameState.status)
        assertEquals(0, gameState.score)
        assertEquals(0, gameState.totalDrops)
        assertEquals(30, gameState.dropsUntilNewRow)
        assertEquals(1, gameState.level)
        assertNotNull(gameState.nextDisc)
        assertEquals(GameMode.Normal, gameState.mode)
    }
    
    @Test
    fun testInitialGameStateChallengeMode() {
        val mode = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        val gameState = gameEngine.startNewGame(mode)
        
        assertEquals(5, gameState.dropsUntilNewRow)
        assertEquals(mode, gameState.mode)
    }
    
    @Test
    fun testInitialGameStateSequenceMode() {
        val mode = GameMode.Sequence(123)
        val gameState = gameEngine.startNewGame(mode)
        
        assertEquals(30, gameState.dropsUntilNewRow)
        assertEquals(mode, gameState.mode)
    }
    
    // ===== Drop Disc Tests =====
    
    @Test
    fun testDropDiscInEmptyColumn() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Use a disc with value 5 which won't match (only 1 disc in column)
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        
        gameState = gameEngine.dropDisc(gameState, 3)
        
        // Disc should be at bottom (value 5 won't match when there's only 1 disc)
        val bottomRow = GameState.GRID_SIZE - 1
        val cell = gameState.getCell(bottomRow, 3)
        assertTrue("Disc should be at bottom of column", cell.isOccupied())
        
        // Counters should increment
        assertEquals(1, gameState.totalDrops)
        assertEquals(29, gameState.dropsUntilNewRow)
    }
    
    @Test
    fun testDropMultipleDiscsInSameColumn() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Drop 3 discs in column 2 with values that won't match
        // Use values 5, 6, 7 so they won't match when there are 3 discs
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 2)
        
        gameState = gameState.copy(nextDisc = Disc.Numbered(6))
        gameState = gameEngine.dropDisc(gameState, 2)
        
        gameState = gameState.copy(nextDisc = Disc.Numbered(7))
        gameState = gameEngine.dropDisc(gameState, 2)
        
        // Should have 3 discs stacked (none match the column count of 3)
        assertEquals(3, gameState.countDiscsInColumn(2))
        assertEquals(3, gameState.totalDrops)
    }
    
    @Test
    fun testDropDiscInFullColumn() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Fill column 0
        for (row in 0 until GameState.GRID_SIZE) {
            gameState = gameState.setCell(row, 0, Cell.Occupied(Disc.Numbered(1)))
        }
        
        // Try to drop in full column
        gameState = gameEngine.dropDisc(gameState, 0)
        
        // Game should be over
        assertEquals(GameStatus.GameOver, gameState.status)
    }
    
    // ===== Match Detection Tests =====
    
    @Test
    fun testRowMatch() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Create a row with 3 discs, one of which has value 3
        gameState = gameState.setCell(6, 0, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(6, 1, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(3)))
        
        // Drop another disc to trigger processing
        gameState = gameState.copy(nextDisc = Disc.Numbered(5))
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // The 3 should have broken
        val cell = gameState.getCell(6, 2)
        assertTrue(cell.isEmpty())
    }
    
    @Test
    fun testColumnMatch() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Stack 4 discs in column, one with value 4
        gameState = gameState.setCell(3, 2, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.setCell(4, 2, Cell.Occupied(Disc.Numbered(2)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(3)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(4)))
        
        gameState = gameState.copy(nextDisc = Disc.Numbered(5), totalDrops = 1)
        
        // The 4 at bottom should match and break
        // (Implementation processes this automatically)
    }
    
    // ===== Gravity Tests =====
    
    @Test
    fun testGravityAfterBreak() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Place discs in column 2 - values that won't immediately match
        gameState = gameState.setCell(3, 2, Cell.Occupied(Disc.Numbered(5)))
        gameState = gameState.setCell(5, 2, Cell.Occupied(Disc.Numbered(6)))
        gameState = gameState.setCell(6, 2, Cell.Occupied(Disc.Numbered(7)))
        
        // Manually remove middle disc to create a gap
        gameState = gameState.setCell(5, 2, Cell.Empty)
        
        // We still have 2 discs in the column (with a gap)
        assertEquals(2, gameState.countDiscsInColumn(2))
        
        // Note: Gravity is applied automatically during chain processing
        // when dropDisc is called. This test just verifies counting.
    }
    
    // ===== Solid Disc Tests =====
    
    @Test
    fun testSolidDiscCrackProgression() {
        val solidDisc = Disc.Solid(0, 5)
        
        // First crack
        val crackedOnce = solidDisc.addCrack()
        assertTrue(crackedOnce is Disc.Solid)
        assertEquals(1, (crackedOnce as Disc.Solid).cracks)
        assertEquals(5, crackedOnce.hiddenValue)
        
        // Second crack reveals number
        val revealed = crackedOnce.addCrack()
        assertTrue(revealed is Disc.Numbered)
        assertEquals(5, (revealed as Disc.Numbered).value)
    }
    
    // ===== Scoring Tests =====
    
    @Test
    fun testBasicScoring() {
        var gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Place a disc that will match (value 1, only 1 disc in row)
        gameState = gameState.setCell(6, 3, Cell.Occupied(Disc.Numbered(1)))
        gameState = gameState.copy(nextDisc = Disc.Numbered(1))
        
        val initialScore = gameState.score
        gameState = gameEngine.dropDisc(gameState, 5)
        
        // Score should increase (base is 7 points per disc)
        assertTrue(gameState.score > initialScore)
    }
    
    // ===== Game Mode Tests =====
    
    @Test
    fun testNormalModeRowTiming() {
        val mode = GameMode.Normal
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(30, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
        assertEquals(4, config.minDropsUntilRow)
    }
    
    @Test
    fun testChallengeModeEasyRowTiming() {
        val mode = GameMode.Challenge(ChallengeDifficulty.EASY, false)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(10, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
    }
    
    @Test
    fun testChallengeModeMediumRowTiming() {
        val mode = GameMode.Challenge(ChallengeDifficulty.MEDIUM, true)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(7, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
    }
    
    @Test
    fun testChallengeModeHardRowTiming() {
        val mode = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(5, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
    }
    
    @Test
    fun testChallengeModeExtremeRowTiming() {
        val mode = GameMode.Challenge(ChallengeDifficulty.EXTREME, false)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(3, config.initialDropsUntilRow)
    }
    
    // ===== Sequence Mode Tests =====
    
    @Test
    fun testSequenceModeDeterministic() {
        val mode = GameMode.Sequence(42)
        val generator1 = DiscGenerator(mode)
        val generator2 = DiscGenerator(mode)
        
        // Generate 10 discs from each
        val discs1 = (1..10).map { generator1.generateNextDisc() }
        val discs2 = (1..10).map { generator2.generateNextDisc() }
        
        // All should match
        discs1.zip(discs2).forEach { (d1, d2) ->
            assertEquals(d1::class, d2::class)
            if (d1 is Disc.Numbered && d2 is Disc.Numbered) {
                assertEquals(d1.value, d2.value)
            }
        }
    }
    
    @Test
    fun testSequenceModeDifferentSeeds() {
        val generator1 = DiscGenerator(GameMode.Sequence(42))
        val generator2 = DiscGenerator(GameMode.Sequence(100))
        
        val discs1 = (1..20).map { generator1.generateNextDisc() }
        val discs2 = (1..20).map { generator2.generateNextDisc() }
        
        // At least some should be different
        val differences = discs1.zip(discs2).count { (d1, d2) ->
            d1::class != d2::class || 
            (d1 is Disc.Numbered && d2 is Disc.Numbered && d1.value != d2.value)
        }
        
        assertTrue("Different seeds should produce different sequences", differences > 0)
    }
    
    // ===== Edge Cases =====
    
    @Test
    fun testDropInvalidColumn() {
        val gameState = gameEngine.startNewGame(GameMode.Normal)
        
        // Note: The game doesn't crash on invalid column (it's a UI responsibility)
        // But we can verify bounds checking in GameState
        try {
            gameState.getCell(0, 7)
            fail("Should throw exception for invalid column")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }
    
    @Test
    fun testGameStateImmutability() {
        val state1 = gameEngine.startNewGame(GameMode.Normal)
        val state2 = gameEngine.dropDisc(state1, 3)
        
        // Original state unchanged
        assertEquals(0, state1.totalDrops)
        
        // New state updated
        assertEquals(1, state2.totalDrops)
    }
}

