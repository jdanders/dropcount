package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for disc generation run-limiting functionality.
 */
class DiscGeneratorRunLimitTest {
    
    @Test
    fun testNormalModeRespectsValueRunLimit() {
        val generator = DiscGenerator(GameMode.Normal)
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Check each sliding window of DISC_RUN_WINDOW_SIZE
        for (i in 0..(generatedDiscs.size - GameConfig.DISC_RUN_WINDOW_SIZE)) {
            val window = generatedDiscs.subList(i, i + GameConfig.DISC_RUN_WINDOW_SIZE)
            
            // Count each value (1-7) in the window
            for (value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
                val count = window.count { disc ->
                    disc.numericValue == value
                }
                
                assertTrue(
                    "Too many discs with value $value in window at position $i: $count (max: ${GameConfig.MAX_SAME_VALUE_IN_WINDOW})",
                    count <= GameConfig.MAX_SAME_VALUE_IN_WINDOW
                )
            }
        }
    }
    
    @Test
    fun testNormalModeRespectsSolidRunLimit() {
        val generator = DiscGenerator(GameMode.Normal)
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Check each sliding window
        for (i in 0..(generatedDiscs.size - GameConfig.DISC_RUN_WINDOW_SIZE)) {
            val window = generatedDiscs.subList(i, i + GameConfig.DISC_RUN_WINDOW_SIZE)
            val solidCount = window.count { it is Disc.Solid }
            
            assertTrue(
                "Too many solid discs in window at position $i: $solidCount (max: ${GameConfig.MAX_SOLID_DISCS_IN_WINDOW})",
                solidCount <= GameConfig.MAX_SOLID_DISCS_IN_WINDOW
            )
        }
    }
    
    @Test
    fun testChallengeModeNoSolidDiscs() {
        val generator = DiscGenerator(GameMode.Challenge(ChallengeDifficulty.HARD))
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Challenge mode should have NO solid discs
        val solidCount = generatedDiscs.count { it is Disc.Solid }
        assertEquals("Challenge mode should not generate solid discs", 0, solidCount)
    }
    
    @Test
    fun testChallengeModeRespectsValueRunLimit() {
        val generator = DiscGenerator(GameMode.Challenge(ChallengeDifficulty.MEDIUM))
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Check each sliding window
        for (i in 0..(generatedDiscs.size - GameConfig.DISC_RUN_WINDOW_SIZE)) {
            val window = generatedDiscs.subList(i, i + GameConfig.DISC_RUN_WINDOW_SIZE)
            
            for (value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
                val count = window.count { disc ->
                    (disc as? Disc.Numbered)?.value == value
                }
                
                assertTrue(
                    "Too many discs with value $value in window at position $i: $count",
                    count <= GameConfig.MAX_SAME_VALUE_IN_WINDOW
                )
            }
        }
    }
    
    @Test
    fun testSequenceModeRespectsRunLimits() {
        val generator = DiscGenerator(GameMode.Sequence(GameConfig.DEFAULT_SEQUENCE_SEED))
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Check both value and solid limits
        for (i in 0..(generatedDiscs.size - GameConfig.DISC_RUN_WINDOW_SIZE)) {
            val window = generatedDiscs.subList(i, i + GameConfig.DISC_RUN_WINDOW_SIZE)
            
            // Check solid limit
            val solidCount = window.count { it is Disc.Solid }
            assertTrue(
                "Too many solid discs in window",
                solidCount <= GameConfig.MAX_SOLID_DISCS_IN_WINDOW
            )
            
            // Check value limits
            for (value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
                val count = window.count { disc ->
                    disc.numericValue == value
                }
                assertTrue(
                    "Too many discs with value $value",
                    count <= GameConfig.MAX_SAME_VALUE_IN_WINDOW
                )
            }
        }
    }
    
    @Test
    fun testGeneratorStillProducesVariety() {
        val generator = DiscGenerator(GameMode.Normal)
        val generatedDiscs = (1..100).map { generator.generateNextDisc() }
        
        // Verify we still get all values despite run limiting
        val numberedValues = generatedDiscs
            .filterIsInstance<Disc.Numbered>()
            .map { it.value }
            .toSet()
        
        val solidValues = generatedDiscs
            .filterIsInstance<Disc.Solid>()
            .map { it.hiddenValue }
            .toSet()
        
        val allValues = numberedValues + solidValues
        
        // Should have generated most values (at least 5 out of 7)
        assertTrue(
            "Generator should produce variety of values, got ${allValues.size} unique values",
            allValues.size >= 5
        )
        
        // Should have mix of numbered and solid
        val hasNumbered = generatedDiscs.any { it is Disc.Numbered }
        val hasSolid = generatedDiscs.any { it is Disc.Solid }
        
        assertTrue("Should generate numbered discs", hasNumbered)
        assertTrue("Should generate solid discs", hasSolid)
    }
    
    @Test
    fun testSequenceModeDeterminismWithRunLimits() {
        // Even with run limits, same seed should produce same sequence
        val generator1 = DiscGenerator(GameMode.Sequence(42))
        val generator2 = DiscGenerator(GameMode.Sequence(42))
        
        val discs1 = (1..50).map { generator1.generateNextDisc() }
        val discs2 = (1..50).map { generator2.generateNextDisc() }
        
        // Sequences should match
        discs1.zip(discs2).forEachIndexed { index, (d1, d2) ->
            assertEquals("Disc type mismatch at position $index", d1::class, d2::class)
            when {
                d1 is Disc.Numbered && d2 is Disc.Numbered -> {
                    assertEquals("Numbered value mismatch at position $index", d1.value, d2.value)
                }
                d1 is Disc.Solid && d2 is Disc.Solid -> {
                    assertEquals("Solid hidden value mismatch at position $index", d1.hiddenValue, d2.hiddenValue)
                }
            }
        }
    }
    
    @Test
    fun testRunLimitConfigurationValues() {
        // Verify configuration makes mathematical sense
        assertTrue(
            "Window size should be larger than max count",
            GameConfig.DISC_RUN_WINDOW_SIZE > GameConfig.MAX_SAME_VALUE_IN_WINDOW
        )
        
        assertTrue(
            "Max same value should be reasonable",
            GameConfig.MAX_SAME_VALUE_IN_WINDOW > 0 && 
            GameConfig.MAX_SAME_VALUE_IN_WINDOW < GameConfig.DISC_RUN_WINDOW_SIZE
        )
        
        assertTrue(
            "Max solid discs should be reasonable",
            GameConfig.MAX_SOLID_DISCS_IN_WINDOW > 0 && 
            GameConfig.MAX_SOLID_DISCS_IN_WINDOW < GameConfig.DISC_RUN_WINDOW_SIZE
        )
    }
}

