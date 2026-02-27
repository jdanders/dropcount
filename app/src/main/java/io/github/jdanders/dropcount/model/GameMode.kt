package io.github.jdanders.dropcount.model

import androidx.annotation.StringRes
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.config.GameConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Represents the different game modes available.
 */
@Serializable
sealed class GameMode {
    /**
     * Normal mode: 30→29→28... drops per row, mixed colored & gray discs.
     */
    @Serializable
    @SerialName("Normal")
    data object Normal : GameMode()
    
    /**
     * Challenge mode: Configurable difficulty, colored discs only.
     * Row timing is always fixed (does not decrease).
     * @param difficulty The difficulty preset
     */
    @Serializable
    @SerialName("Challenge")
    data class Challenge(
        val difficulty: ChallengeDifficulty
    ) : GameMode()
    
    /**
     * Sequence mode: Same as Normal but with deterministic disc generation.
     * @param seed The seed for deterministic random generation
     */
    @Serializable
    @SerialName("Sequence")
    data class Sequence(val seed: Long = GameConfig.DEFAULT_SEQUENCE_SEED) : GameMode()
}

/**
 * Challenge mode difficulty presets.
 */
@Serializable
enum class ChallengeDifficulty(val dropsPerRow: Int, @param:StringRes val displayNameRes: Int) {
    EASY(GameConfig.CHALLENGE_EASY_DROPS_PER_ROW, R.string.difficulty_easy),
    MEDIUM(GameConfig.CHALLENGE_MEDIUM_DROPS_PER_ROW, R.string.difficulty_medium),
    HARD(GameConfig.CHALLENGE_HARD_DROPS_PER_ROW, R.string.difficulty_hard),
    EXTREME(GameConfig.CHALLENGE_EXTREME_DROPS_PER_ROW, R.string.difficulty_extreme)
}

/**
 * Configuration for row timing based on game mode.
 */
data class RowTimingConfig(
    val initialDropsUntilRow: Int,
    val shouldDecrement: Boolean,
    val minDropsUntilRow: Int = GameConfig.MIN_CHAIN_LEVEL
) {
    companion object {
        fun fromGameMode(mode: GameMode): RowTimingConfig {
            return when (mode) {
                is GameMode.Normal -> RowTimingConfig(
                    initialDropsUntilRow = GameConfig.NORMAL_MODE_INITIAL_DROPS_PER_ROW,
                    shouldDecrement = true,
                    minDropsUntilRow = GameConfig.NORMAL_MODE_MIN_DROPS_PER_ROW
                )
                is GameMode.Challenge -> {
                    val initial = mode.difficulty.dropsPerRow
                    RowTimingConfig(
                        initialDropsUntilRow = initial,
                        shouldDecrement = false, // Always fixed for Challenge mode
                        minDropsUntilRow = initial
                    )
                }
                is GameMode.Sequence -> RowTimingConfig(
                    initialDropsUntilRow = GameConfig.NORMAL_MODE_INITIAL_DROPS_PER_ROW,
                    shouldDecrement = true,
                    minDropsUntilRow = GameConfig.NORMAL_MODE_MIN_DROPS_PER_ROW
                )
            }
        }
    }
}

