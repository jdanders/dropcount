package io.github.jdanders.dropcount.engine

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.model.Disc
import io.github.jdanders.dropcount.model.GameMode
import kotlin.random.Random

/**
 * Generates discs based on the game mode with run-limiting to prevent frustrating streaks.
 */
class DiscGenerator(private val mode: GameMode) {
    private var random: Random = when (mode) {
        is GameMode.Sequence -> Random(mode.seed)
        else -> Random.Default
    }

    // Track recent discs to prevent excessive runs
    private val recentDiscs = mutableListOf<Disc>()

    /**
     * Gets the history of recent discs (for saving state).
     */
    fun getRecentDiscs(): List<Disc> = recentDiscs.toList()

    /**
     * Sets the history of recent discs (for restoring state).
     */
    fun setRecentDiscs(discs: List<Disc>) {
        recentDiscs.clear()
        recentDiscs.addAll(discs)
    }

    /**
     * Sets the random seed. Used for restoring generator state (e.g., for undo).
     */
    fun setSeed(seed: Long) {
        random = Random(seed)
    }

    /**
     * Generates the next disc based on the game mode rules.
     * Prevents excessive runs of the same value or type.
     */
    fun generateNextDisc(): Disc {
        val maxAttempts = GameConfig.MAX_GENERATION_ATTEMPTS // Prevent infinite loops
        var attempts = 0

        // Determine if solid discs are allowed based on game mode
        val allowSolidDiscs = when (mode) {
            is GameMode.Challenge -> false  // Challenge mode doesn't allow solid discs during gameplay
            else -> true  // Normal and Sequence allow solid discs
        }

        while (attempts < maxAttempts) {
            val candidate = generateRandomDisc(allowSolidDiscs)

            if (isDiscAcceptable(candidate)) {
                recentDiscs.add(candidate)
                // Keep only the last DISC_RUN_WINDOW_SIZE discs
                if (recentDiscs.size > GameConfig.DISC_RUN_WINDOW_SIZE) {
                    recentDiscs.removeAt(0)
                }
                return candidate
            }

            attempts++
        }

        // Fallback: if we can't find an acceptable disc after many attempts,
        // just return a disc that's least represented in the window
        val fallback = generateLeastRepresentedDisc()
        recentDiscs.add(fallback)
        if (recentDiscs.size > GameConfig.DISC_RUN_WINDOW_SIZE) {
            recentDiscs.removeAt(0)
        }
        return fallback
    }

    /**
     * Generates a random disc without checking run constraints.
     * @param allowSolidDiscs Whether solid discs are allowed to be generated
     */
    private fun generateRandomDisc(allowSolidDiscs: Boolean): Disc {
        return if (allowSolidDiscs && random.nextFloat() >= GameConfig.NUMBERED_DISC_PROBABILITY) {
            // Generate solid disc
            Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, random.nextInt(),
                      random.nextInt(GameConfig.MIN_DISC_VALUE, GameConfig.MAX_DISC_VALUE + 1))
        } else {
            // Generate numbered disc
            Disc.Numbered(random.nextInt(GameConfig.MIN_DISC_VALUE, GameConfig.MAX_DISC_VALUE + 1))
        }
    }

    /**
     * Checks if a disc would violate run constraints.
     */
    private fun isDiscAcceptable(disc: Disc): Boolean {
        // Check solid disc limit
        if (disc is Disc.Solid) {
            val solidCount = recentDiscs.count { it is Disc.Solid }
            if (solidCount >= GameConfig.MAX_SOLID_DISCS_IN_WINDOW) {
                return false
            }
        }

        // Check same-value limit (for numbered discs and hidden values in solids)
        val discValue = disc.numericValue

        val sameValueCount = recentDiscs.count { recentDisc ->
            recentDisc.numericValue == discValue
        }

        if (sameValueCount >= GameConfig.MAX_SAME_VALUE_IN_WINDOW) {
            return false
        }

        return true
    }

    /**
     * Generates the disc that's least represented in the recent window.
     * Used as a fallback when normal generation fails.
     */
    private fun generateLeastRepresentedDisc(): Disc {
        // Count solid discs
        val solidCount = recentDiscs.count { it is Disc.Solid }

        // Count each value (1-7)
        val valueCounts = (GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE).associateWith { value ->
            recentDiscs.count { disc ->
                disc.numericValue == value
            }
        }

        val leastUsedValue = valueCounts.minByOrNull { it.value }?.key ?: GameConfig.MIN_DISC_VALUE

        // Prefer numbered disc if we have too many solids, otherwise follow normal probability
        val shouldBeNumbered = when (mode) {
            is GameMode.Challenge -> true
            else -> solidCount >= GameConfig.MAX_SOLID_DISCS_IN_WINDOW ||
                    random.nextFloat() < GameConfig.NUMBERED_DISC_PROBABILITY
        }

        return if (shouldBeNumbered) {
            Disc.Numbered(leastUsedValue)
        } else {
            Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, random.nextInt(), leastUsedValue)
        }
    }

    /**
     * Generates a disc for initial board setup (bypasses run constraints).
     * This ensures we get a good mix of disc types for the starting board.
     */
    fun generateInitialDisc(): Disc {
        return generateRandomDisc(allowSolidDiscs = true)
    }

    /**
     * Generates a row of discs for when a new row appears.
     * New rows are ALWAYS entirely solid (gray) discs.
     */
    fun generateNewRow(): List<Disc> {
        // All GRID_SIZE positions get solid discs with random hidden values
        return List(GameConfig.GRID_SIZE) {
            Disc.Solid(GameConfig.SOLID_DISC_INITIAL_CRACKS, random.nextInt(), random.nextInt(GameConfig.MIN_DISC_VALUE, GameConfig.MAX_DISC_VALUE + 1))
        }
    }
}
