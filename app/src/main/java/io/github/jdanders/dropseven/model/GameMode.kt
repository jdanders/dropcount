package io.github.jdanders.dropseven.model

/**
 * Represents the different game modes available.
 */
sealed class GameMode {
    /**
     * Normal mode: 30→29→28... drops per row, mixed colored & gray discs.
     */
    data object Normal : GameMode()
    
    /**
     * Challenge mode: Configurable difficulty, colored discs only.
     * @param difficulty The difficulty preset
     * @param isDecreasing If true, drop count decreases like Normal mode
     */
    data class Challenge(
        val difficulty: ChallengeDifficulty,
        val isDecreasing: Boolean
    ) : GameMode()
    
    /**
     * Sequence mode: Same as Normal but with deterministic disc generation.
     * @param seed The seed for deterministic random generation
     */
    data class Sequence(val seed: Long = 42) : GameMode()
}

/**
 * Challenge mode difficulty presets.
 */
enum class ChallengeDifficulty(val dropsPerRow: Int, val displayName: String) {
    EASY(10, "Easy"),
    MEDIUM(7, "Medium"),
    HARD(5, "Hard"),
    EXTREME(3, "Extreme")
}

/**
 * Configuration for row timing based on game mode.
 */
data class RowTimingConfig(
    val initialDropsUntilRow: Int,
    val shouldDecrement: Boolean,
    val minDropsUntilRow: Int = 1
) {
    companion object {
        fun fromGameMode(mode: GameMode): RowTimingConfig {
            return when (mode) {
                is GameMode.Normal -> RowTimingConfig(
                    initialDropsUntilRow = 30,
                    shouldDecrement = true,
                    minDropsUntilRow = 4
                )
                is GameMode.Challenge -> {
                    val initial = mode.difficulty.dropsPerRow
                    RowTimingConfig(
                        initialDropsUntilRow = initial,
                        shouldDecrement = mode.isDecreasing,
                        minDropsUntilRow = minOf(initial, 4) // Don't set min higher than initial
                    )
                }
                is GameMode.Sequence -> RowTimingConfig(
                    initialDropsUntilRow = 30,
                    shouldDecrement = true,
                    minDropsUntilRow = 4
                )
            }
        }
    }
}

