package io.github.jdanders.dropcount.ui.components

import androidx.compose.ui.geometry.Offset

/**
 * Configuration parameters for rendering a disc in the game grid.
 */
data class DiscRenderConfig(
    val center: Offset,
    val radius: Float,
    val alpha: Float = 1f,
    val isHighlighted: Boolean = false,
    val highlightValue: Int? = null,
    val isGameOverHighlight: Boolean = false
)