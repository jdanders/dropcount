package io.github.jdanders.dropseven

import io.github.jdanders.dropseven.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for GameMode and related classes.
 */
class GameModeTest {
    
    // ===== GameMode Tests =====
    
    @Test
    fun testNormalModeCreation() {
        val mode = GameMode.Normal
        assertNotNull(mode)
    }
    
    @Test
    fun testChallengeModeCreation() {
        for (difficulty in ChallengeDifficulty.entries) {
            for (isDecreasing in listOf(true, false)) {
                val mode = GameMode.Challenge(difficulty, isDecreasing)
                assertEquals(difficulty, mode.difficulty)
                assertEquals(isDecreasing, mode.isDecreasing)
            }
        }
    }
    
    @Test
    fun testSequenceModeCreation() {
        val mode1 = GameMode.Sequence()
        assertEquals(42, mode1.seed)
        
        val mode2 = GameMode.Sequence(12345)
        assertEquals(12345, mode2.seed)
    }
    
    @Test
    fun testGameModeEquality() {
        val normal1 = GameMode.Normal
        val normal2 = GameMode.Normal
        assertEquals(normal1, normal2)
        
        val challenge1 = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        val challenge2 = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        assertEquals(challenge1, challenge2)
        
        val sequence1 = GameMode.Sequence(100)
        val sequence2 = GameMode.Sequence(100)
        assertEquals(sequence1, sequence2)
    }
    
    @Test
    fun testGameModeInequality() {
        val normal = GameMode.Normal
        val challenge = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        val sequence = GameMode.Sequence(42)
        
        assertNotEquals(normal, challenge)
        assertNotEquals(normal, sequence)
        assertNotEquals(challenge, sequence)
    }
    
    // ===== ChallengeDifficulty Tests =====
    
    @Test
    fun testChallengeDifficultyValues() {
        assertEquals(10, ChallengeDifficulty.EASY.dropsPerRow)
        assertEquals(7, ChallengeDifficulty.MEDIUM.dropsPerRow)
        assertEquals(5, ChallengeDifficulty.HARD.dropsPerRow)
        assertEquals(3, ChallengeDifficulty.EXTREME.dropsPerRow)
    }
    
    @Test
    fun testChallengeDifficultyDisplayNames() {
        assertEquals("Easy", ChallengeDifficulty.EASY.displayName)
        assertEquals("Medium", ChallengeDifficulty.MEDIUM.displayName)
        assertEquals("Hard", ChallengeDifficulty.HARD.displayName)
        assertEquals("Extreme", ChallengeDifficulty.EXTREME.displayName)
    }
    
    @Test
    fun testAllDifficultiesExist() {
        val difficulties = ChallengeDifficulty.entries
        assertEquals(4, difficulties.size)
        assertTrue(difficulties.contains(ChallengeDifficulty.EASY))
        assertTrue(difficulties.contains(ChallengeDifficulty.MEDIUM))
        assertTrue(difficulties.contains(ChallengeDifficulty.HARD))
        assertTrue(difficulties.contains(ChallengeDifficulty.EXTREME))
    }
    
    @Test
    fun testDifficultyOrdering() {
        // Easier should have more drops per row
        assertTrue(ChallengeDifficulty.EASY.dropsPerRow > ChallengeDifficulty.MEDIUM.dropsPerRow)
        assertTrue(ChallengeDifficulty.MEDIUM.dropsPerRow > ChallengeDifficulty.HARD.dropsPerRow)
        assertTrue(ChallengeDifficulty.HARD.dropsPerRow > ChallengeDifficulty.EXTREME.dropsPerRow)
    }
    
    // ===== RowTimingConfig Tests =====
    
    @Test
    fun testRowTimingConfigForNormalMode() {
        val config = RowTimingConfig.fromGameMode(GameMode.Normal)
        
        assertEquals(30, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
        assertEquals(4, config.minDropsUntilRow)
    }
    
    @Test
    fun testRowTimingConfigForChallengeEasy() {
        val mode = GameMode.Challenge(ChallengeDifficulty.EASY, false)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(10, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
        assertEquals(4, config.minDropsUntilRow)
    }
    
    @Test
    fun testRowTimingConfigForChallengeMediumDecreasing() {
        val mode = GameMode.Challenge(ChallengeDifficulty.MEDIUM, true)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(7, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
    }
    
    @Test
    fun testRowTimingConfigForChallengeHardFixed() {
        val mode = GameMode.Challenge(ChallengeDifficulty.HARD, false)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(5, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
    }
    
    @Test
    fun testRowTimingConfigForChallengeExtremeDecreasing() {
        val mode = GameMode.Challenge(ChallengeDifficulty.EXTREME, true)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(3, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
    }
    
    @Test
    fun testRowTimingConfigForSequenceMode() {
        val config = RowTimingConfig.fromGameMode(GameMode.Sequence(100))
        
        assertEquals(30, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
        assertEquals(4, config.minDropsUntilRow)
    }
    
    @Test
    fun testRowTimingConfigForAllDifficulties() {
        for (difficulty in ChallengeDifficulty.entries) {
            for (isDecreasing in listOf(true, false)) {
                val mode = GameMode.Challenge(difficulty, isDecreasing)
                val config = RowTimingConfig.fromGameMode(mode)
                
                assertEquals(difficulty.dropsPerRow, config.initialDropsUntilRow)
                assertEquals(isDecreasing, config.shouldDecrement)
                // Min should be min(initial, 4) to avoid setting min higher than initial
                val expectedMin = minOf(difficulty.dropsPerRow, 4)
                assertEquals(expectedMin, config.minDropsUntilRow)
            }
        }
    }
    
    // ===== Integration Tests =====
    
    @Test
    fun testModesSupportAllRequiredFeatures() {
        val modes = listOf(
            GameMode.Normal,
            GameMode.Challenge(ChallengeDifficulty.EASY, false),
            GameMode.Challenge(ChallengeDifficulty.MEDIUM, true),
            GameMode.Challenge(ChallengeDifficulty.HARD, false),
            GameMode.Challenge(ChallengeDifficulty.EXTREME, true),
            GameMode.Sequence(42),
            GameMode.Sequence(100)
        )
        
        modes.forEach { mode ->
            // Each mode should have valid row timing config
            val config = RowTimingConfig.fromGameMode(mode)
            assertTrue("Initial drops should be positive", 
                config.initialDropsUntilRow > 0)
            assertTrue("Min drops should be positive", 
                config.minDropsUntilRow > 0)
            assertTrue("Initial should be >= min", 
                config.initialDropsUntilRow >= config.minDropsUntilRow)
        }
    }
}

