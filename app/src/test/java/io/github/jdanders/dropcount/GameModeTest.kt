package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.model.*
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
            val mode = GameMode.Challenge(difficulty)
            assertEquals(difficulty, mode.difficulty)
        }
    }
    
    @Test
    fun testSequenceModeCreation() {
        val mode1 = GameMode.Sequence()
        assertEquals(GameConfig.DEFAULT_SEQUENCE_SEED, mode1.seed)
        
        val mode2 = GameMode.Sequence(12345)
        assertEquals(12345, mode2.seed)
    }
    
    @Test
    fun testGameModeEquality() {
        val normal1 = GameMode.Normal
        val normal2 = GameMode.Normal
        assertEquals(normal1, normal2)
        
        val challenge1 = GameMode.Challenge(ChallengeDifficulty.HARD)
        val challenge2 = GameMode.Challenge(ChallengeDifficulty.HARD)
        assertEquals(challenge1, challenge2)
        
        val sequence1 = GameMode.Sequence(100)
        val sequence2 = GameMode.Sequence(100)
        assertEquals(sequence1, sequence2)
    }
    
    @Test
    fun testGameModeInequality() {
        val normal = GameMode.Normal
        val challenge = GameMode.Challenge(ChallengeDifficulty.HARD)
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
        assertEquals(R.string.difficulty_easy, ChallengeDifficulty.EASY.displayNameRes)
        assertEquals(R.string.difficulty_medium, ChallengeDifficulty.MEDIUM.displayNameRes)
        assertEquals(R.string.difficulty_hard, ChallengeDifficulty.HARD.displayNameRes)
        assertEquals(R.string.difficulty_extreme, ChallengeDifficulty.EXTREME.displayNameRes)
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
        assertEquals(5, config.minDropsUntilRow)
    }
    
    @Test
    fun testRowTimingConfigForChallengeEasy() {
        val mode = GameMode.Challenge(ChallengeDifficulty.EASY)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(10, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
        assertEquals(10, config.minDropsUntilRow)
    }

    @Test
    fun testRowTimingConfigForChallengeHardFixed() {
        val mode = GameMode.Challenge(ChallengeDifficulty.HARD)
        val config = RowTimingConfig.fromGameMode(mode)
        
        assertEquals(5, config.initialDropsUntilRow)
        assertFalse(config.shouldDecrement)
    }
    
    @Test
    fun testRowTimingConfigForSequenceMode() {
        val config = RowTimingConfig.fromGameMode(GameMode.Sequence(100))
        
        assertEquals(30, config.initialDropsUntilRow)
        assertTrue(config.shouldDecrement)
        assertEquals(5, config.minDropsUntilRow)
    }
    
    @Test
    fun testRowTimingConfigForAllDifficulties() {
        for (difficulty in ChallengeDifficulty.entries) {
            val mode = GameMode.Challenge(difficulty)
            val config = RowTimingConfig.fromGameMode(mode)
            
            assertEquals(difficulty.dropsPerRow, config.initialDropsUntilRow)
            assertFalse("Challenge mode should always have fixed timing", config.shouldDecrement)
            // Min should equal initial for Challenge mode (fixed timing)
            assertEquals(difficulty.dropsPerRow, config.minDropsUntilRow)
        }
    }
    
    // ===== Integration Tests =====
    
    @Test
    fun testModesSupportAllRequiredFeatures() {
        val modes = listOf(
            GameMode.Normal,
            GameMode.Challenge(ChallengeDifficulty.EASY),
            GameMode.Challenge(ChallengeDifficulty.MEDIUM),
            GameMode.Challenge(ChallengeDifficulty.HARD),
            GameMode.Challenge(ChallengeDifficulty.EXTREME),
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

