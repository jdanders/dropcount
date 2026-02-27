package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.model.Disc
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Disc class functionality.
 */
class DiscTest {
    
    @Test
    fun testNumberedDiscCreation() {
        val disc = Disc.Numbered(5)
        assertEquals(5, disc.value)
        assertTrue(disc.isNumbered())
        assertEquals(5, disc.getNumber())
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testNumberedDiscInvalidValueTooLow() {
        Disc.Numbered(GameConfig.MIN_DISC_VALUE - 1)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testNumberedDiscInvalidValueTooHigh() {
        Disc.Numbered(GameConfig.MAX_DISC_VALUE + 1)
    }
    
    @Test
    fun testNumberedDiscValidRange() {
        for (value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
            val disc = Disc.Numbered(value)
            assertEquals(value, disc.value)
        }
    }
    
    @Test
    fun testSolidDiscCreation() {
        val disc = Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, 0, 3)
        assertEquals(GameConfig.SOLID_DISC_INITIAL_CRACKS, disc.cracks)
        assertEquals(3, disc.hiddenValue)
        assertFalse(disc.isNumbered())
        assertNull(disc.getNumber())
    }
    
    @Test
    fun testSolidDiscFirstCrack() {
        val disc = Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, 0, 5)
        val cracked = disc.addCrack()
        
        assertTrue(cracked is Disc.Solid)
        assertEquals(1, (cracked as Disc.Solid).cracks)
        assertEquals(5, cracked.hiddenValue)
        assertFalse(cracked.isFullyCracked)
    }
    
    @Test
    fun testSolidDiscSecondCrackRevealsNumber() {
        val disc = Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, 0, 5)
        val crackedOnce = disc.addCrack()
        val revealed = (crackedOnce as Disc.Solid).addCrack()
        
        assertTrue(revealed is Disc.Numbered)
        assertEquals(5, (revealed as Disc.Numbered).value)
        assertTrue(revealed.isNumbered())
    }
    
    @Test
    fun testSolidDiscAllStates() {
        val initialDisc = Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, 0, GameConfig.MAX_DISC_VALUE)
        
        // State 1: Solid (initial cracks)
        assertEquals(GameConfig.SOLID_DISC_INITIAL_CRACKS, initialDisc.cracks)
        assertFalse(initialDisc.isFullyCracked)
        
        // State 2: Cracked (1 crack)
        val crackedOnce = initialDisc.addCrack() as Disc.Solid
        assertEquals(1, crackedOnce.cracks)
        assertFalse(crackedOnce.isFullyCracked)
        
        // State 3: Revealed (numbered disc)
        val revealed = crackedOnce.addCrack() as Disc.Numbered
        assertEquals(GameConfig.MAX_DISC_VALUE, revealed.value)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testSolidDiscInvalidCracksCount() {
        Disc.Solid(GameConfig.SOLID_DISC_CRACKS_TO_REVEAL + 1, 0, 5)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testSolidDiscInvalidHiddenValue() {
        Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, 0, GameConfig.MAX_DISC_VALUE + 1)
    }
    
    @Test
    fun testSolidDiscAddCrackAlreadyFullyCracked() {
        val disc = Disc.Solid(GameConfig.SOLID_DISC_CRACKS_TO_REVEAL, 0, 5)
        val result = disc.addCrack()
        
        // Should return itself when already fully cracked
        assertEquals(disc, result)
    }
}

