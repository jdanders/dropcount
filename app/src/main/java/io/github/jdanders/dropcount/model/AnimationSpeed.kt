package io.github.jdanders.dropcount.model

import androidx.annotation.StringRes
import io.github.jdanders.dropcount.R
import kotlinx.serialization.Serializable

/**
 * Animation speed presets for gameplay animations.
 * Match highlighting can be slow/medium/fast.
 * Other animations (drop, gravity, new row) cannot be slower than medium.
 */
@Serializable
enum class AnimationSpeed(val multiplier: Float, @param:StringRes val displayNameRes: Int) {
    /**
     * Slow animations - 2x duration (0.5x speed)
     */
    SLOW(0.5f, R.string.speed_slow),

    /**
     * Medium animations - normal duration (1.0x speed)
     */
    MEDIUM(1.0f, R.string.speed_medium),

    /**
     * Fast animations - 0.5x duration (2.0x speed)
     */
    FAST(2.0f, R.string.speed_fast);

    /**
     * Calculates the actual duration for an animation based on this speed setting.
     * @param baseDuration The base duration in milliseconds at normal speed
     * @return The adjusted duration in milliseconds
     */
    fun adjustDuration(baseDuration: Long): Long {
        return (baseDuration / multiplier).toLong()
    }

    /**
     * Calculates the actual duration for an animation based on this speed setting.
     * @param baseDuration The base duration in milliseconds at normal speed
     * @return The adjusted duration in milliseconds
     */
    fun adjustDuration(baseDuration: Int): Int {
        return (baseDuration / multiplier).toInt()
    }

    /**
     * Returns the effective multiplier for non-match animations.
     * Ensures animations can't be slower than medium speed.
     */
    private fun getNonMatchMultiplier(): Float {
        return when (this) {
            SLOW -> 1.0f  // Treat SLOW as MEDIUM for non-match animations
            MEDIUM -> 1.0f
            FAST -> 2.0f
        }
    }

    /**
     * Calculates duration for non-match animations (drop, gravity, new row).
     * These animations cannot be slower than medium speed.
     */
    fun adjustNonMatchDuration(baseDuration: Long): Long {
        return (baseDuration / getNonMatchMultiplier()).toLong()
    }

    fun adjustNonMatchDuration(baseDuration: Int): Int {
        return (baseDuration / getNonMatchMultiplier()).toInt()
    }
}
