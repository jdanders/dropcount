package io.github.jdanders.dropcount.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import io.github.jdanders.dropcount.model.Disc

/**
 * Interface for theme-specific rendering implementations.
 * Each theme provides its own visual interpretation of game elements.
 */
interface ThemeRenderer {
    
    /**
     * Draws a disc (numbered or solid) at the specified position.
     * 
     * @param disc The disc to draw (Numbered or Solid)
     * @param center Center position of the disc
     * @param radius Radius of the disc
     * @param textMeasurer Text measurer for drawing numbers
     * @param isHighlighted Whether the disc is currently highlighted (match animation)
     * @param alpha Overall transparency (0.0-1.0)
     * @param highlightValue The value to use for highlight color (if different from disc value)
     * @param isGameOverHighlight Whether this is a game-over highlight (red)
     */
    fun DrawScope.drawDisc(
        disc: Disc,
        center: Offset,
        radius: Float,
        textMeasurer: TextMeasurer,
        isHighlighted: Boolean = false,
        alpha: Float = 1f,
        highlightValue: Int? = null,
        isGameOverHighlight: Boolean = false
    )
    
    /**
     * Draws the grid background (behind all cells).
     */
    fun DrawScope.drawGridBackground(size: Size, cellSize: Float)
    
    /**
     * Draws the grid lines that separate cells.
     */
    fun DrawScope.drawGridLines(size: Size, cellSize: Float)
    
    /**
     * Draws the background of a single cell.
     * 
     * @param topLeft Top-left corner of the cell
     * @param size Size of the cell
     * @param isHighlighted Whether the cell is part of a match
     * @param isHovered Whether the column is being hovered
     * @param highlightColor Color to use for highlighting (if isHighlighted)
     */
    fun DrawScope.drawCellBackground(
        topLeft: Offset,
        size: Size,
        isHighlighted: Boolean,
        isHovered: Boolean,
        highlightColor: Color?
    )
    
    /**
     * Returns the text style for drawing disc numbers.
     * Should scale based on disc radius.
     * 
     * @param radius The radius of the disc in pixels
     * @param density The screen density for coordinate conversion
     */
    fun getDiscTextStyle(radius: Float, density: Density): TextStyle
    
    /**
     * Returns the color for a numbered disc value (1-7).
     */
    fun getDiscColor(value: Int): Color
    
    /**
     * Returns the background gradient for screens.
     */
    fun getBackgroundGradient(): Brush
    
    /**
     * Returns the color for a solid disc based on crack state.
     * 
     * @param cracks Number of cracks (0, 1, or 2)
     */
    fun getSolidDiscColor(cracks: Int): Color
    
    /**
     * Returns the grid line color.
     */
    fun getGridLineColor(): Color
    
    /**
     * Returns the score display color.
     */
    /**
     * Returns the score display color.
     */
    fun getScoreColor(): Color
    
    /**
     * Returns the high score display color.
     */
    fun getHighScoreColor(): Color
    
    /**
     * Returns the background color for overlays (Game Over, Paused).
     */
    fun getOverlayBackgroundColor(): Color
    
    /**
     * Returns the color for label text (SCORE, HIGH SCORE, LEVEL, etc.).
     */
    fun getLabelTextColor(): Color

    /**
     * Returns the background color for cards/buttons in this theme.
     */
    fun getCardBackgroundColor(): Color

    /**
     * Returns the border color for cards/buttons in this theme.
     */
    fun getCardBorderColor(): Color
    
    /**
     * Draws a disc that is currently dropping/animating.
     * Allows themes to add trails or motion blur effects.
     * 
     * @param disc The disc being dropped
     * @param center Current center position
     * @param radius Radius of the disc
     * @param textMeasurer TextMeasurer for numbers
     * @param effectiveProgress 0.0 to 1.0 progress of the animation
     * @param startY Starting Y position of the drop
     * @param endY Ending Y position of the drop (Visual destination)
     * @param cellSize Size of the grid cell
     */
    fun DrawScope.drawDroppingDisc(
        disc: Disc,
        center: Offset,
        radius: Float,
        textMeasurer: TextMeasurer,
        effectiveProgress: Float,
        startY: Float,
        endY: Float,
        cellSize: Float
    )
}
