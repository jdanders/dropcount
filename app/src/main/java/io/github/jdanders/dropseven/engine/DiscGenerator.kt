package io.github.jdanders.dropseven.engine

import io.github.jdanders.dropseven.model.Disc
import io.github.jdanders.dropseven.model.GameMode
import kotlin.random.Random

/**
 * Generates discs based on the game mode.
 */
class DiscGenerator(private val mode: GameMode) {
    private val random: Random = when (mode) {
        is GameMode.Sequence -> Random(mode.seed)
        else -> Random.Default
    }
    
    /**
     * Generates the next disc based on the game mode rules.
     */
    fun generateNextDisc(): Disc {
        return when (mode) {
            is GameMode.Normal, is GameMode.Sequence -> {
                // 70% numbered, 30% solid
                if (random.nextFloat() < 0.7f) {
                    Disc.Numbered(random.nextInt(1, 8))
                } else {
                    Disc.Solid(0, random.nextInt(1, 8))
                }
            }
            is GameMode.Challenge -> {
                // 100% numbered (colored only)
                Disc.Numbered(random.nextInt(1, 8))
            }
        }
    }
    
    /**
     * Generates a row of discs for when a new row appears.
     * New rows are ALWAYS entirely solid (gray) discs.
     */
    fun generateNewRow(): List<Disc> {
        // All 7 positions get solid discs with random hidden values
        return List(7) { 
            Disc.Solid(0, random.nextInt(1, 8))
        }
    }
}

