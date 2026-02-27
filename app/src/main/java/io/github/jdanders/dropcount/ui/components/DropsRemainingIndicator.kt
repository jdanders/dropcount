package io.github.jdanders.dropcount.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.jdanders.dropcount.config.GameConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import io.github.jdanders.dropcount.ui.theme.*

/**
 * Visual indicator showing the number of drops remaining until a new row appears.
 * Displays mini-discs in a fixed-width row. Filled discs show remaining drops,
 * hollow discs show used drops.
 * 
 * @param dropsRemaining The number of drops remaining
 * @param maxDrops The maximum number of drops for the current game mode (determines how many discs to show)
 * @param modifier Modifier for the component
 */
@Composable
fun DropsRemainingIndicator(
    dropsRemaining: Int,
    maxDrops: Int,
    themeRenderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    when (themeRenderer) {
        is NeonThemeRenderer -> NeonDropsRemainingIndicator(dropsRemaining, maxDrops, themeRenderer, modifier)
        is WoodblockThemeRenderer -> WoodblockDropsRemainingIndicator(dropsRemaining, maxDrops, themeRenderer, modifier)
        else -> ClassicDropsRemainingIndicator(dropsRemaining, maxDrops, themeRenderer, modifier)
    }
}

@Composable
private fun ClassicDropsRemainingIndicator(
    dropsRemaining: Int,
    maxDrops: Int,
    themeRenderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Clamp dropsRemaining to valid range (0-maxDrops)
    val clampedDrops = dropsRemaining.coerceIn(0, maxDrops)
    val clampedMaxDrops = maxDrops.coerceIn(1, GameConfig.MAX_DROPS_INDICATOR_COUNT)
    
    // Convert dp to pixels
    val discSizePx = with(density) { GameConfig.DROPS_INDICATOR_DISC_SIZE_DP.dp.toPx() }
    
    // Optional: Color coding based on remaining drops (relative to maxDrops)
    val colorCodedDiscColor = remember(clampedDrops, clampedMaxDrops, themeRenderer) {
        val percentage = if (clampedMaxDrops > 0) clampedDrops.toFloat() / clampedMaxDrops else 0f
        when {
            percentage >= 0.66f -> themeRenderer.getDiscColor(5).copy(alpha = GameConfig.DROPS_INDICATOR_ALPHA)
            percentage >= 0.33f -> themeRenderer.getDiscColor(3).copy(alpha = GameConfig.DROPS_INDICATOR_ALPHA)
            clampedDrops > 0 -> themeRenderer.getDiscColor(1).copy(alpha = GameConfig.DROPS_INDICATOR_ALPHA)
            else -> Color.White.copy(alpha = GameConfig.DROPS_INDICATOR_ALPHA)
        }
    }
    
    Row(
        modifier = modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw discs based on maxDrops for the current mode - filled for remaining, hollow for used
        repeat(clampedMaxDrops) { index ->
            val isRemaining = index < clampedDrops
            
            // Calculate opacity gradient (leftmost = full opacity, rightmost = slightly faded)
            val opacityMultiplier = if (clampedDrops > 0 && isRemaining) {
                1f - (index.toFloat() / clampedDrops) * 0.3f // Fade from 1.0 to 0.7
            } else {
                1f
            }
            
            val discColor = if (isRemaining) {
                colorCodedDiscColor.copy(alpha = GameConfig.DROPS_INDICATOR_ALPHA * opacityMultiplier)
            } else {
                // Hollow disc color - not used, will be drawn as outline only
                Color.White // Color doesn't matter for hollow, outline will be drawn
            }
            
            if (index > 0) {
                Spacer(modifier = Modifier.width(GameConfig.DROPS_INDICATOR_SPACING_DP.dp))
            }
            
            Canvas(
                modifier = Modifier.size(GameConfig.DROPS_INDICATOR_DISC_SIZE_DP.dp)
            ) {
                drawMiniDisc(
                    color = discColor,
                    radius = discSizePx / 2f,
                    themeRenderer = themeRenderer,
                    isHollow = !isRemaining
                )
            }
        }
    }
}

@Composable
private fun NeonDropsRemainingIndicator(
    dropsRemaining: Int,
    maxDrops: Int,
    themeRenderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    val percentage = dropsRemaining.toFloat() / maxDrops.toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(MidnightVoid.copy(alpha = 0.3f))
            .border(1.dp, GridLineGlow, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
    ) {
        // Background grid pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val segmentWidth = size.width / maxDrops
            for (i in 0 until maxDrops) {
                drawLine(
                    color = GridLineNeon,
                    start = Offset(i * segmentWidth, 0f),
                    end = Offset(i * segmentWidth, size.height),
                    strokeWidth = 0.5f
                )
            }
        }

        // Fill bar with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(percentage)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            when {
                                percentage > 0.66f -> LimeBeam
                                percentage > 0.33f -> AmberAlert
                                else -> Color(0xFFFF1744)
                            }.copy(alpha = 0.6f),
                            when {
                                percentage > 0.66f -> LimeBeam
                                percentage > 0.33f -> AmberAlert
                                else -> Color(0xFFFF1744)
                            }
                        )
                    )
                )
        )
    }
}

@Composable
private fun WoodblockDropsRemainingIndicator(
    dropsRemaining: Int,
    maxDrops: Int,
    themeRenderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val clampedDrops = dropsRemaining.coerceIn(0, maxDrops)
    val clampedMaxDrops = maxDrops.coerceIn(1, GameConfig.MAX_DROPS_INDICATOR_COUNT)
    
    // Brush stroke style indicator
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(
                color = WoodblockPaper,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = WoodblockInk.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
    ) {
        // Background brush strokes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeSpacing = size.width / clampedMaxDrops
            val random = kotlin.random.Random(12345)
            
            for (i in 0 until clampedMaxDrops) {
                val x = i * strokeSpacing + strokeSpacing / 2
                val isActive = i < clampedDrops
                
                // Draw brush stroke mark
                val strokeHeight = size.height * 0.6f
                val strokeTop = (size.height - strokeHeight) / 2
                
                val path = Path().apply {
                    val wobble = (random.nextFloat() - 0.5f) * 2f
                    moveTo(x + wobble, strokeTop)
                    lineTo(x - wobble, strokeTop + strokeHeight)
                }
                
                drawPath(
                    path = path,
                    color = if (isActive) {
                        WoodblockVermilion.copy(alpha = 0.8f)
                    } else {
                        WoodblockInk.copy(alpha = 0.15f)
                    },
                    style = Stroke(
                        width = if (isActive) 4f else 2f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

/**
 * Draws a mini-disc indicator.
 * 
 * @param color The color of the disc
 * @param radius The radius of the disc
 * @param isHollow If true, draws only the outline (hollow disc). If false, draws filled disc.
 */
private fun DrawScope.drawMiniDisc(
    color: Color,
    radius: Float,
    themeRenderer: ThemeRenderer,
    isHollow: Boolean = false
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    
    if (isHollow) {
        // Draw hollow disc (outline only) with strong contrast
        drawCircle(
            color = Color.White.copy(alpha = 0.8f), // Strong white outline for contrast
            radius = radius,
            center = center,
            style = Stroke(width = 2f) // Thicker stroke for better visibility
        )
    } else {
        // Draw filled disc
        drawCircle(
            color = color,
            radius = radius,
            center = center
        )
        
        // Add a subtle border/stroke for better visibility
        drawCircle(
            color = themeRenderer.getGridLineColor().copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 1f)
        )
    }
}
