package io.github.jdanders.dropcount.util

import io.github.jdanders.dropcount.model.Disc

/**
 * Extension properties for Disc operations.
 */

/**
 * Returns true if this disc can be broken (i.e., is not already fully broken).
 */
val Disc.isBreakable: Boolean
    get() = when (this) {
        is Disc.Numbered -> false // Numbered discs are already "broken"
        is Disc.Solid -> !this.isFullyCracked
    }

/**
 * Returns true if this disc is solid (has cracks remaining).
 */
val Disc.isSolid: Boolean
    get() = this is Disc.Solid

/**
 * Returns the display text for this disc (number for numbered, "S" + cracks for solid).
 */
val Disc.displayText: String
    get() = when (this) {
        is Disc.Numbered -> numericValue.toString()
        is Disc.Solid -> "S$cracks"
    }