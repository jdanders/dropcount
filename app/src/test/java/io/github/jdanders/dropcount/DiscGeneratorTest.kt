package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for DiscGenerator functionality.
 */
class DiscGeneratorTest {

    // ===== Normal Mode Tests =====

    @Test
    fun testNormalModeGeneratesValidDiscs() {
        val generator = DiscGenerator(GameMode.Normal)

        repeat(100) {
            val disc = generator.generateNextDisc()
            assertTrue("Disc should be Numbered or Solid",
                disc is Disc.Numbered || disc is Disc.Solid)

            if (disc is Disc.Numbered) {
                assertTrue("Numbered disc value should be ${GameConfig.MIN_DISC_VALUE}-${GameConfig.MAX_DISC_VALUE}",
                    disc.value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE)
            }

            if (disc is Disc.Solid) {
                assertEquals("Solid disc should start with ${GameConfig.SOLID_DISC_INITIAL_CRACKS} cracks",
                    GameConfig.SOLID_DISC_INITIAL_CRACKS, disc.cracks)
                assertTrue("Hidden value should be ${GameConfig.MIN_DISC_VALUE}-${GameConfig.MAX_DISC_VALUE}",
                    disc.hiddenValue in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE)
            }
        }
    }

    @Test
    fun testNormalModeGeneratesMixOfDiscTypes() {
        val generator = DiscGenerator(GameMode.Normal)

        val discs = (1..100).map { generator.generateNextDisc() }
        val numberedCount = discs.count { it is Disc.Numbered }
        val solidCount = discs.count { it is Disc.Solid }

        // Should have both types (with high probability)
        assertTrue("Should generate some numbered discs", numberedCount > 0)
        assertTrue("Should generate some solid discs", solidCount > 0)

        // Approximately 70/30 split (allowing variance)
        val numberedPercentage = numberedCount.toDouble() / 100.0
        assertTrue("Numbered discs should be roughly 70%",
            numberedPercentage in 0.55..0.9)
    }

    @Test
    fun testNormalModeGeneratesAllNumbers() {
        val generator = DiscGenerator(GameMode.Normal)

        val numberedDiscs = (1..200)
            .map { generator.generateNextDisc() }
            .filterIsInstance<Disc.Numbered>()

        val uniqueValues = numberedDiscs.map { it.value }.toSet()

        // Should generate all values 1-7 eventually
        assertTrue("Should generate variety of values", uniqueValues.size >= 5)
    }

    // ===== Challenge Mode Tests =====

    @Test
    fun testChallengeModeGeneratesOnlyNumberedDiscs() {
        val mode = GameMode.Challenge(ChallengeDifficulty.HARD)
        val generator = DiscGenerator(mode)

        repeat(100) {
            val disc = generator.generateNextDisc()
            assertTrue("Challenge mode should only generate Numbered discs",
                disc is Disc.Numbered)

            val numbered = disc as Disc.Numbered
            assertTrue("Value should be ${GameConfig.MIN_DISC_VALUE}-${GameConfig.MAX_DISC_VALUE}",
                numbered.value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE)
        }
    }

    @Test
    fun testChallengeModeAllDifficulties() {
        for (difficulty in ChallengeDifficulty.entries) {
            val mode = GameMode.Challenge(difficulty)
            val generator = DiscGenerator(mode)

            val discs = (1..50).map { generator.generateNextDisc() }

            // All should be numbered
            assertEquals("All discs should be numbered", 50,
                discs.count { it is Disc.Numbered })
        }
    }

    // ===== Sequence Mode Tests =====

    @Test
    fun testSequenceModeIsDeterministic() {
        val seed = GameConfig.DEFAULT_SEQUENCE_SEED
        val generator1 = DiscGenerator(GameMode.Sequence(seed))
        val generator2 = DiscGenerator(GameMode.Sequence(seed))

        repeat(50) {
            val disc1 = generator1.generateNextDisc()
            val disc2 = generator2.generateNextDisc()

            assertEquals("Types should match", disc1::class, disc2::class)

            when {
                disc1 is Disc.Numbered && disc2 is Disc.Numbered -> {
                    assertEquals("Values should match", disc1.value, disc2.value)
                }
                disc1 is Disc.Solid && disc2 is Disc.Solid -> {
                    assertEquals("Cracks should match", disc1.cracks, disc2.cracks)
                    assertEquals("Hidden values should match",
                        disc1.hiddenValue, disc2.hiddenValue)
                }
            }
        }
    }

    @Test
    fun testSequenceModeDifferentSeedsProduceDifferentSequences() {
        val generator1 = DiscGenerator(GameMode.Sequence(100))
        val generator2 = DiscGenerator(GameMode.Sequence(200))

        val discs1 = (1..30).map { generator1.generateNextDisc() }
        val discs2 = (1..30).map { generator2.generateNextDisc() }

        val differences = discs1.zip(discs2).count { (d1, d2) ->
            d1::class != d2::class ||
            (d1 is Disc.Numbered && d2 is Disc.Numbered && d1.value != d2.value) ||
            (d1 is Disc.Solid && d2 is Disc.Solid && d1.hiddenValue != d2.hiddenValue)
        }

        assertTrue("Different seeds should produce different sequences (found $differences differences)",
            differences > 5)
    }

    @Test
    fun testSequenceModeGeneratesMixedTypes() {
        val generator = DiscGenerator(GameMode.Sequence(GameConfig.DEFAULT_SEQUENCE_SEED))

        val discs = (1..100).map { generator.generateNextDisc() }
        val numberedCount = discs.count { it is Disc.Numbered }
        val solidCount = discs.count { it is Disc.Solid }

        assertTrue("Should have some numbered discs", numberedCount > 0)
        assertTrue("Should have some solid discs", solidCount > 0)
    }

    // ===== New Row Generation Tests =====

    @Test
    fun testGenerateNewRowCreatesValidRow() {
        val generator = DiscGenerator(GameMode.Normal)
        val row = generator.generateNewRow()

        assertEquals("Row should have ${GameConfig.GRID_SIZE} positions", GameConfig.GRID_SIZE, row.size)

        row.forEach { disc ->
            assertTrue("Disc should be valid type",
                disc is Disc.Numbered || disc is Disc.Solid)
        }
    }

    @Test
    fun testGenerateNewRowIsFullyFilled() {
        val generator = DiscGenerator(GameMode.Normal)
        val row = generator.generateNewRow()

        // New rows are always completely filled with solid discs
        assertEquals("Row should be fully filled", GameConfig.GRID_SIZE, row.size)
        assertTrue("All discs should be solid", row.all { it is Disc.Solid })
    }

    @Test
    fun testGenerateNewRowForAllModes() {
        val modes = listOf(
            GameMode.Normal,
            GameMode.Challenge(ChallengeDifficulty.HARD),
            GameMode.Sequence(GameConfig.DEFAULT_SEQUENCE_SEED)
        )

        modes.forEach { mode ->
            val generator = DiscGenerator(mode)
            val row = generator.generateNewRow()

            // All modes generate rows with all solid discs
            assertEquals("Row should be fully filled", GameConfig.GRID_SIZE, row.size)
            assertTrue("All discs should be solid", row.all { it is Disc.Solid })
        }
    }

    @Test
    fun testGenerateNewRowMultipleTimes() {
        val generator = DiscGenerator(GameMode.Normal)

        repeat(10) {
            val row = generator.generateNewRow()
            assertEquals(GameConfig.GRID_SIZE, row.size)

            // Verify each position
            row.forEachIndexed { index, disc ->
                disc?.let {
                    assertTrue("Disc at position $index should be valid",
                        it is Disc.Numbered || it is Disc.Solid)
                }
            }
        }
    }

    // ===== Edge Cases =====

    @Test
    fun testGeneratorDoesNotCrash() {
        for (difficulty in ChallengeDifficulty.entries) {
            val modes = listOf(
                GameMode.Normal,
            GameMode.Challenge(difficulty),
            GameMode.Sequence(System.currentTimeMillis())
            )

            modes.forEach { mode ->
                val generator = DiscGenerator(mode)

                // Generate many discs without crashing
                repeat(100) {
                    generator.generateNextDisc()
                    generator.generateNewRow()
                }
            }
        }
    }

    @Test
    fun testMultipleGeneratorsIndependent() {
        val gen1 = DiscGenerator(GameMode.Normal)
        val gen2 = DiscGenerator(GameMode.Normal)

        // Generate from gen1
        val discs1 = (1..10).map { gen1.generateNextDisc() }

        // Generate from gen2 (should have its own random state)
        val discs2 = (1..10).map { gen2.generateNextDisc() }

        // They should likely be different (not guaranteed but extremely likely)
        val allIdentical = discs1.zip(discs2).all { (d1, d2) ->
            d1::class == d2::class &&
            (d1 as? Disc.Numbered)?.value == (d2 as? Disc.Numbered)?.value
        }

        assertFalse("Two independent generators should produce different sequences",
            allIdentical)
    }
}
