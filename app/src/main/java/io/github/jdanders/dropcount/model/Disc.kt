package io.github.jdanders.dropcount.model

import io.github.jdanders.dropcount.config.GameConfig
import kotlinx.serialization.Serializable

/**
 * Represents a disc in the game.
 */
@Serializable
sealed class Disc {
    abstract val numericValue: Int
    /**
     * A numbered disc (${GameConfig.MIN_DISC_VALUE}-${GameConfig.MAX_DISC_VALUE}) that breaks when the count in its row or column matches its value.
     */
    @Serializable
    data class Numbered(val value: Int) : Disc() {
        init {
            require(value in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
                "Disc value must be between ${GameConfig.MIN_DISC_VALUE} and ${GameConfig.MAX_DISC_VALUE}"
            }
        }

        override val numericValue: Int get() = value
    }

    /**
     * A solid disc that needs to be cracked twice before revealing a number.
     * @param cracks Number of times adjacent discs have broken (0, 1, or 2)
     * @param hiddenValue The number that will be revealed when fully cracked
     */
    @Serializable
    data class Solid(
        val cracks: Int = GameConfig.SOLID_DISC_INITIAL_CRACKS,
        val crackSeed: Int,
        val hiddenValue: Int = GameConfig.MIN_DISC_VALUE) : Disc() {
        init {
            require(cracks in GameConfig.SOLID_DISC_INITIAL_CRACKS..GameConfig.SOLID_DISC_CRACKS_TO_REVEAL) {
                "Cracks must be ${GameConfig.SOLID_DISC_INITIAL_CRACKS}, 1, or ${GameConfig.SOLID_DISC_CRACKS_TO_REVEAL}"
            }
            require(hiddenValue in GameConfig.MIN_DISC_VALUE..GameConfig.MAX_DISC_VALUE) {
                "Hidden value must be between ${GameConfig.MIN_DISC_VALUE} and ${GameConfig.MAX_DISC_VALUE}"
            }
        }

        override val numericValue: Int get() = hiddenValue

        /**
         * Returns a new Solid disc with incremented crack count.
         * If fully cracked (${GameConfig.SOLID_DISC_CRACKS_TO_REVEAL} cracks), returns a Numbered disc.
         */
        fun addCrack(): Disc {
            return when (cracks) {
                GameConfig.SOLID_DISC_INITIAL_CRACKS -> Solid(1, crackSeed, hiddenValue)
                1 -> Numbered(hiddenValue)
                else -> this // Already fully cracked
            }
        }

        val isFullyCracked: Boolean
            get() = cracks >= GameConfig.SOLID_DISC_CRACKS_TO_REVEAL
    }

    /**
     * Returns true if this is a numbered disc (including revealed solid discs).
     */
    fun isNumbered(): Boolean = this is Numbered

    /**
     * Gets the visible number if this is a numbered disc, or null otherwise.
     */
    fun getNumber(): Int? = (this as? Numbered)?.value
}
