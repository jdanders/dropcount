package io.github.jdanders.dropcount.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.model.Disc
import kotlin.random.Random

class FoundryThemeRenderer : ThemeRenderer {

    private var noisePositions: List<Offset>? = null
    private var lastNoiseSize: Size? = null

    private fun getNoisePositions(size: Size): List<Offset> {
        if (noisePositions == null || lastNoiseSize != size) {
            val random = Random(12345)
            noisePositions = List(ThemeConfig.Foundry.NOISE_COUNT) {
                Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
            }
            lastNoiseSize = size
        }
        return noisePositions!!
    }

    override fun DrawScope.drawDisc(
        disc: Disc,
        center: Offset,
        radius: Float,
        textMeasurer: TextMeasurer,
        isHighlighted: Boolean,
        alpha: Float,
        highlightValue: Int?,
        isGameOverHighlight: Boolean
    ) {
        val size = radius * 2
        val halfSize = size / 2f
        val topLeft = Offset(center.x - halfSize, center.y - halfSize)
        val actualSize = size // In foundry guide, they used size * 0.85f, but here radius is usually half cell size. 
                              // we will stick to radius * 2 as the full bounds and maybe inset slightly if needed.
                              // actually, let's use the full cell for the "heavy" look, or slightly inset.
        val drawSize = size * ThemeConfig.Foundry.DISC_SIZE_FACTOR
        val drawTopLeft = Offset(center.x - drawSize / 2f, center.y - drawSize / 2f)

        when (disc) {
            is Disc.Numbered -> {
                val value = highlightValue ?: disc.value
                val grayIndex = (value - 1).coerceIn(0, 6)
                val baseColor = when(grayIndex) {
                    0 -> DiscGray1
                    1 -> DiscGray2
                    2 -> DiscGray3
                    3 -> DiscGray4
                    4 -> DiscGray5
                    5 -> DiscGray6
                    else -> DiscGray7
                }

                // Deep shadow (offset)
                drawRect(
                    color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.DISC_SHADOW_ALPHA * alpha),
                    topLeft = drawTopLeft + Offset(ThemeConfig.Foundry.DISC_SHADOW_OFFSET, ThemeConfig.Foundry.DISC_SHADOW_OFFSET),
                    size = Size(drawSize, drawSize)
                )

                // Main square
                drawRect(
                    color = baseColor.copy(alpha = alpha),
                    topLeft = drawTopLeft,
                    size = Size(drawSize, drawSize)
                )

                // Top highlight (foundry bevel)
                drawRect(
                    color = StarkWhite.copy(alpha = ThemeConfig.Foundry.BEVEL_HIGHLIGHT_ALPHA * alpha),
                    topLeft = drawTopLeft,
                    size = Size(drawSize, ThemeConfig.Foundry.BEVEL_SIZE)
                )
                drawRect(
                    color = StarkWhite.copy(alpha = ThemeConfig.Foundry.BEVEL_HIGHLIGHT_ALPHA * alpha),
                    topLeft = drawTopLeft,
                    size = Size(ThemeConfig.Foundry.BEVEL_SIZE, drawSize)
                )

                // Bottom shadow (foundry bevel)
                drawRect(
                    color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.BEVEL_SHADOW_ALPHA * alpha),
                    topLeft = drawTopLeft + Offset(0f, drawSize - ThemeConfig.Foundry.BEVEL_SIZE),
                    size = Size(drawSize, ThemeConfig.Foundry.BEVEL_SIZE)
                )
                drawRect(
                    color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.BEVEL_SHADOW_ALPHA * alpha),
                    topLeft = drawTopLeft + Offset(drawSize - ThemeConfig.Foundry.BEVEL_SIZE, 0f),
                    size = Size(ThemeConfig.Foundry.BEVEL_SIZE, drawSize)
                )

                // Outer border (sharp edge)
                drawRect(
                    color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.NUMBERED_BORDER_ALPHA * alpha),
                    topLeft = drawTopLeft,
                    size = Size(drawSize, drawSize),
                    style = Stroke(width = ThemeConfig.Foundry.BORDER_WIDTH)
                )

                // Number (bold, centered)
                val textStyle = getDiscTextStyle(radius, this).copy(color = StarkWhite.copy(alpha = alpha))
                
                val textLayoutResult = textMeasurer.measure(
                    text = value.toString(),
                    style = textStyle
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        center.x - textLayoutResult.size.width / 2,
                        center.y - textLayoutResult.size.height / 2
                    )
                )

                // Highlight state
                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) IndustrialOrange else StarkWhite
                    drawRect(
                        color = highlightColor.copy(alpha = ThemeConfig.Foundry.HIGHLIGHT_ALPHA),
                        topLeft = drawTopLeft - Offset(ThemeConfig.Foundry.HIGHLIGHT_PADDING, ThemeConfig.Foundry.HIGHLIGHT_PADDING),
                        size = Size(drawSize + ThemeConfig.Foundry.HIGHLIGHT_PADDING * 2, drawSize + ThemeConfig.Foundry.HIGHLIGHT_PADDING * 2),
                        style = Stroke(width = ThemeConfig.Foundry.HIGHLIGHT_WIDTH)
                    )
                }
            }
            is Disc.Solid -> {
                 // Deep shadow
                drawRect(
                    color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.DISC_SHADOW_ALPHA * alpha), // Slightly darker for solid? No, sticking to config or using same.
                    topLeft = drawTopLeft + Offset(ThemeConfig.Foundry.DISC_SHADOW_OFFSET, ThemeConfig.Foundry.DISC_SHADOW_OFFSET),
                    size = Size(drawSize, drawSize)
                )

                // Main dark square
                val solidColor = getSolidDiscColor(disc.cracks)
                drawRect(
                    color = solidColor.copy(alpha = alpha),
                    topLeft = drawTopLeft,
                    size = Size(drawSize, drawSize)
                )

                // Diagonal stripes pattern (industrial hazard)
                val stripeSpacing = drawSize / 6f
                // We need to clip to the rect for the stripes, but since we are drawing lines, manual clipping or careful drawing is needed.
                // Simple approach: draw lines within bounds.
                // Or just use the loop from design guide
                 for (i in -6..6) {
                    val startX = i * stripeSpacing
                    // This logic from guide assumes drawing on infinite canvas or clipped. 
                    // To keep it simple and safe without clip(), we can skip it or try to implement carefully.
                    // Let's implement a simplified version or use clipRect if possible. 
                    // DrawScope has clipRect.
                     // DrawScope has clipRect.
                }
                // Using clipRect for stripes
                // We are already in DrawScope, so we can call clipRect directly.
                clipRect(
                    left = drawTopLeft.x,
                    top = drawTopLeft.y,
                    right = drawTopLeft.x + drawSize,
                    bottom = drawTopLeft.y + drawSize
                ) {
                    for (i in -ThemeConfig.Foundry.STRIPE_COUNT..ThemeConfig.Foundry.STRIPE_COUNT) {
                        val startX = drawTopLeft.x + i * stripeSpacing
                        val stripeColor = if (disc.cracks > 0) SolidDiscMid else IndustrialWhite
                        drawLine(
                            color = stripeColor.copy(alpha = ThemeConfig.Foundry.STRIPE_ALPHA * alpha),
                            start = Offset(startX, drawTopLeft.y),
                            end = Offset(startX + drawSize, drawTopLeft.y + drawSize),
                            strokeWidth = ThemeConfig.Foundry.STRIPE_STROKE_WIDTH
                        )
                    }
                }

                // Crack lines (if cracked)
                if (disc.cracks > 0) {
                if (disc.cracks > 0) {
                     drawStructuralX(
                        topLeft = drawTopLeft,
                        size = drawSize,
                        alpha = alpha
                    )
                }
                }

                // Border - Light outline for visibility
                drawRect(
                    color = IndustrialWhite.copy(alpha = ThemeConfig.Foundry.SOLID_BORDER_ALPHA * alpha),
                    topLeft = drawTopLeft,
                    size = Size(drawSize, drawSize),
                    style = Stroke(width = ThemeConfig.Foundry.BORDER_WIDTH)
                )

                if (isHighlighted) {
                    drawRect(
                        color = StarkWhite.copy(alpha = ThemeConfig.Foundry.HIGHLIGHT_ALPHA),
                        topLeft = drawTopLeft - Offset(ThemeConfig.Foundry.HIGHLIGHT_PADDING, ThemeConfig.Foundry.HIGHLIGHT_PADDING),
                        size = Size(drawSize + ThemeConfig.Foundry.HIGHLIGHT_PADDING * 2, drawSize + ThemeConfig.Foundry.HIGHLIGHT_PADDING * 2),
                        style = Stroke(width = ThemeConfig.Foundry.HIGHLIGHT_WIDTH)
                    )
                }
            }
        }
    }

    private fun DrawScope.drawStructuralX(
        topLeft: Offset,
        size: Float,
        alpha: Float
    ) {
        // Line 1: Top-left to Bottom-right
        drawLine(
            color = ConcreteBlack.copy(alpha = ThemeConfig.Foundry.STRUCTURAL_X_ALPHA * alpha),
            start = topLeft,
            end = topLeft + Offset(size, size),
            strokeWidth = ThemeConfig.Foundry.STRUCTURAL_X_WIDTH,
            cap = androidx.compose.ui.graphics.StrokeCap.Square
        )

        // Line 2: Top-right to Bottom-left
        drawLine(
            color = ConcreteBlack.copy(alpha = ThemeConfig.Foundry.STRUCTURAL_X_ALPHA * alpha),
            start = topLeft + Offset(size, 0f),
            end = topLeft + Offset(0f, size),
            strokeWidth = ThemeConfig.Foundry.STRUCTURAL_X_WIDTH,
            cap = androidx.compose.ui.graphics.StrokeCap.Square
        )
        
        // Stenciled look: Gap in middle? 
        // No, construction X needs to be solid. 
        // Maybe a slight highlight on the edges for depth?
        // Keeping it simple and foundry for now.
    }


    override fun DrawScope.drawGridBackground(size: Size, cellSize: Float) {
        // Base concrete texture
        drawRect(
            color = ConcreteBlack,
            topLeft = Offset.Zero,
            size = size
        )

        // Subtle noise texture (simulated)
        val positions = getNoisePositions(size)
        positions.forEach { pos ->
             drawCircle(
                color = Color.White.copy(alpha = ThemeConfig.Foundry.NOISE_ALPHA),
                radius = ThemeConfig.Foundry.NOISE_RADIUS,
                center = pos
            )
        }

        // Heavy outer frame (foundry border)
        drawRect(
            color = ShadowBlack,
            topLeft = Offset(-ThemeConfig.Foundry.OUTER_FRAME_WIDTH, -ThemeConfig.Foundry.OUTER_FRAME_WIDTH),
            size = Size(size.width + ThemeConfig.Foundry.OUTER_FRAME_WIDTH * 2, size.height + ThemeConfig.Foundry.OUTER_FRAME_WIDTH * 2),
            style = Stroke(width = ThemeConfig.Foundry.OUTER_FRAME_WIDTH)
        )

        drawRect(
            color = GridStroke,
            topLeft = Offset(-ThemeConfig.Foundry.OUTER_FRAME_WIDTH/2, -ThemeConfig.Foundry.OUTER_FRAME_WIDTH/2),
            size = Size(size.width + ThemeConfig.Foundry.OUTER_FRAME_WIDTH, size.height + ThemeConfig.Foundry.OUTER_FRAME_WIDTH),
            style = Stroke(width = ThemeConfig.Foundry.OUTER_FRAME_WIDTH/2)
        )
    }

    override fun DrawScope.drawGridLines(size: Size, cellSize: Float) {
         // GRID LINES - Heavy, structural
         // Helper function to draw lines
        val cols = (size.width / cellSize).toInt()
        val rows = (size.height / cellSize).toInt()

        for (i in 0..cols) {
            val pos = i * cellSize
             drawLine(
                color = GridStroke,
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = ThemeConfig.Foundry.BORDER_WIDTH
            )
        }
        for (i in 0..rows) {
            val pos = i * cellSize
             drawLine(
                color = GridStroke,
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = ThemeConfig.Foundry.BORDER_WIDTH
            )
        }

        // Corner emphasis (foundry detail)
         for (row in 0..rows) {
            for (col in 0..cols) {
                val x = col * cellSize
                val y = row * cellSize
                
                drawRect(
                    color = StarkWhite.copy(alpha = ThemeConfig.Foundry.CORNER_DOT_ALPHA),
                    topLeft = Offset(x - ThemeConfig.Foundry.CORNER_DOT_SIZE / 2f, y - ThemeConfig.Foundry.CORNER_DOT_SIZE / 2f),
                    size = Size(ThemeConfig.Foundry.CORNER_DOT_SIZE, ThemeConfig.Foundry.CORNER_DOT_SIZE)
                )
            }
        }
    }

    override fun DrawScope.drawCellBackground(
        topLeft: Offset,
        size: Size,
        isHighlighted: Boolean,
        isHovered: Boolean,
        highlightColor: Color?
    ) {
         // Cell background - subtle depth
        // Always draw base concrete dark
        drawRect(
            color = ConcreteDark,
            topLeft = topLeft,
            size = size
        )

        // Overlay highlight or hover
        if (isHighlighted) {
            drawRect(
                color = StarkWhite.copy(alpha = ThemeConfig.Foundry.CELL_HIGHLIGHT_ALPHA),
                topLeft = topLeft,
                size = size
            )
        } else if (isHovered) {
             drawRect(
                color = ConcreteMid.copy(alpha = ThemeConfig.Foundry.CELL_HOVER_ALPHA),
                topLeft = topLeft,
                size = size
            )
        }

        // Inner shadow (depth effect)
        drawRect(
            color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.INNER_SHADOW_ALPHA),
            topLeft = topLeft,
            size = Size(size.width, ThemeConfig.Foundry.CELL_INNER_SHADOW)
        )
        drawRect(
            color = ShadowBlack.copy(alpha = ThemeConfig.Foundry.INNER_SHADOW_ALPHA),
            topLeft = topLeft,
            size = Size(ThemeConfig.Foundry.CELL_INNER_SHADOW, size.height)
        )
    }

    override fun getDiscTextStyle(radius: Float, density: Density): TextStyle {
        return TextStyle(
            fontFamily = FontFamily.SansSerif, // Fallback to system sans-serif as requested
            fontWeight = FontWeight.Bold,
            fontSize = with(density) { (radius * 2 * ThemeConfig.Foundry.FONT_SCALE_FACTOR).toSp() },
            letterSpacing = 0.sp
        )
    }

    override fun getDiscColor(value: Int): Color {
        val grayIndex = (value - 1).coerceIn(0, 6)
        return when(grayIndex) {
            0 -> DiscGray1
            1 -> DiscGray2
            2 -> DiscGray3
            3 -> DiscGray4
            4 -> DiscGray5
            5 -> DiscGray6
            else -> DiscGray7
        }
    }

    override fun getBackgroundGradient(): Brush {
         return Brush.verticalGradient(
            colors = listOf(ConcreteBlack, ConcreteDark)
        )
    }

    override fun getSolidDiscColor(cracks: Int): Color {
        return if (cracks > 0) SolidDiscFractured else SolidDiscDark
    }

    override fun getGridLineColor(): Color {
        return GridStroke
    }

    override fun getScoreColor(): Color {
        return StarkWhite
    }

    override fun getHighScoreColor(): Color {
        return IndustrialOrangeDesaturated
    }

    override fun getOverlayBackgroundColor(): Color {
        // Semi-transparent concrete
        return ConcreteBlack.copy(alpha = ThemeConfig.Foundry.OVERLAY_ALPHA)
    }
    
    override fun getLabelTextColor(): Color = StarkWhite.copy(alpha = 0.7f)

    override fun getCardBackgroundColor(): Color = ConcreteMid

    override fun getCardBorderColor(): Color = GridStroke
    
    override fun DrawScope.drawDroppingDisc(
        disc: Disc,
        center: Offset,
        radius: Float,
        textMeasurer: TextMeasurer,
        effectiveProgress: Float,
        startY: Float,
        endY: Float,
        cellSize: Float
    ) {
        // For foundry theme, maybe a motion blur trail?
        // Or just the disc itself.
        // Let's just draw the disc for now, to be safe.
        drawDisc(disc, center, radius, textMeasurer)
    }
}
