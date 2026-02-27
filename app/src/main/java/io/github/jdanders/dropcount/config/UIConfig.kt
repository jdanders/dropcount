package io.github.jdanders.dropcount.config

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * UI-specific configuration constants that don't fit in GameConfig.
 * These are used for visual styling and animations in the UI components.
 */
object UIConfig {
    // Grid rendering constants
    const val GRID_STROKE_WIDTH = 2f
    const val GRID_CELL_PADDING = 2f
    const val DISC_OUTLINE_STROKE_WIDTH = 2f

    // Level up animation
    const val LEVEL_UP_ANIMATION_DELAY = 100L
    const val LEVEL_UP_ANIMATION_DURATION = 500
    const val LEVEL_UP_FONT_SIZE = 24

    // Floating point text animation
    const val FLOATING_TEXT_ANIMATION_DELAY = 50L
    const val FLOATING_TEXT_OFFSET_Y = -60f
    const val FLOATING_TEXT_DURATION = 1000
    const val FLOATING_TEXT_FONT_SIZE = 16
    const val FLOATING_TEXT_OUTLINE_WIDTH = 2f

    // Responsive Typography
    /**
     * Calculates a font size for the main title that fits within the available width.
     * @param containerWidth The available width in DP
     * @param baseFontSize The preferred font size for standard screens (360dp width)
     * @return The scaled font size in SP
     */
    fun calculateTitleFontSize(containerWidth: Float, baseFontSize: Float = 56f): TextUnit {
        // Standard phone width is approx 360dp
        val scaleFactor = (containerWidth / 360f).coerceIn(0.6f, 1.5f)
        return (baseFontSize * scaleFactor).sp
    }
}