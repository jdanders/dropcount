package io.github.jdanders.dropcount.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.model.Disc
import io.github.jdanders.dropcount.config.ThemeConfig
import kotlin.math.cos
import kotlin.math.sin

class NeonThemeRenderer : ThemeRenderer {

    private var techPatternPath: Path? = null
    private var lastCachedRadius = 0f
    private val crackPathCache = mutableMapOf<Int, Path>()

    private fun getTechPatternPath(radius: Float): Path {
        if (lastCachedRadius != radius || techPatternPath == null) {
            lastCachedRadius = radius
            crackPathCache.clear()
            techPatternPath = Path().apply {
                for (i in 0..5) {
                    val angle = i * Math.PI / 3
                    val x = (radius * 0.6f * cos(angle)).toFloat()
                    val y = (radius * 0.6f * sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
        }
        return techPatternPath!!
    }

    private fun getNeonCrackPath(seed: Int, radius: Float): Path {
        if (lastCachedRadius != radius) {
            lastCachedRadius = radius
            crackPathCache.clear()
        }
        return crackPathCache.getOrPut(seed) {
            generateNeonCrackPath(seed, radius)
        }
    }

    private fun generateNeonCrackPath(seed: Int, radius: Float): Path {
        val random = kotlin.random.Random(seed)
        val fullPath = Path()
        
        repeat(8 + random.nextInt(5)) {
            var currentPos = Offset(
                (random.nextFloat() - 0.5f) * radius * 0.4f,
                (random.nextFloat() - 0.5f) * radius * 0.4f
            )
            fullPath.moveTo(currentPos.x, currentPos.y)
            
            val angle = random.nextFloat() * 6.28f
            var dir = Offset(cos(angle), sin(angle))
            
            var dist = 0f
            while (dist < radius * 0.9f) {
                val turn = (random.nextFloat() - 0.5f) * 1.5f
                val newAngle = kotlin.math.atan2(dir.y, dir.x) + turn
                dir = Offset(cos(newAngle), sin(newAngle))
                val step = radius * (0.15f + random.nextFloat() * 0.1f)
                currentPos += dir * step
                fullPath.lineTo(currentPos.x, currentPos.y)
                dist += step
            }
        }
        return fullPath
    }

    // Fonts
    // Note: In a real app we'd load these properly, but for now we'll use fallback/system fonts 
    // styled to look techy if custom fonts aren't available yet or use what we have.
    // Ideally we should add the font files, but I'll use Monospace for now as a safe default
    // matching the "IBM Plex Mono" feel.
    private val discFontFamily = FontFamily.Monospace

    // Disc Colors
    private val discColors = mapOf(
        1 to Color(0xFFFF1744), // Hot Red
        2 to Color(0xFFFF9100), // Vibrant Orange
        3 to Color(0xFFFFEA00), // Electric Yellow
        4 to Color(0xFF00E676), // Neon Green
        5 to Color(0xFF00E5FF), // Cyan Bright
        6 to Color(0xFF3D5AFE), // Electric Blue
        7 to Color(0xFFE040FB)  // Magenta Pink
    )

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
        when (disc) {
            is Disc.Numbered -> {
                val color = getDiscColor(disc.numericValue)

                // Outer glow (larger, very faint)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = ThemeConfig.Neon.OUTER_GLOW_ALPHA * alpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * ThemeConfig.Neon.OUTER_GLOW_RADIUS_MULT
                    ),
                    radius = radius * ThemeConfig.Neon.OUTER_GLOW_RADIUS_MULT,
                    center = center
                )

                // Mid glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = ThemeConfig.Neon.MID_GLOW_ALPHA * alpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * ThemeConfig.Neon.MID_GLOW_RADIUS_MULT
                    ),
                    radius = radius * ThemeConfig.Neon.MID_GLOW_RADIUS_MULT,
                    center = center
                )

                // Core disc with gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = ThemeConfig.Neon.CORE_GLOW_ALPHA * alpha),
                            color.copy(alpha = 0.8f * alpha)
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Specular highlight (top-left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = ThemeConfig.Neon.SPECULAR_ALPHA * alpha),
                            Color.Transparent
                        ),
                        center = center + Offset(-radius * 0.3f, -radius * 0.3f),
                        radius = radius * 0.4f
                    ),
                    radius = radius * 0.4f,
                    center = center + Offset(-radius * 0.3f, -radius * 0.3f)
                )

                // Sharp edge highlight
                drawCircle(
                    color = Color.White.copy(alpha = ThemeConfig.Neon.EDGE_HIGHLIGHT_ALPHA * alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2f)
                )

                // Number with glow
                val textStyle = getDiscTextStyle(radius, this).copy(
                    shadow = Shadow(
                        color = color.copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                )

                val textLayoutResult = textMeasurer.measure(
                    text = disc.numericValue.toString(),
                    style = textStyle
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        center.x - textLayoutResult.size.width / 2,
                        center.y - textLayoutResult.size.height / 2
                    ),
                    color = Color.White.copy(alpha = alpha)
                )

                                // Highlight pulse
                                if (isHighlighted) {
                                    val highlightColor = if (isGameOverHighlight) Color.Red else Color.White
                                    drawCircle(
                                        color = highlightColor.copy(alpha = 0.3f),
                                        radius = radius * ThemeConfig.Neon.HIGHLIGHT_RADIUS_MULTIPLIER,
                                        center = center,
                                        style = Stroke(width = ThemeConfig.Neon.HIGHLIGHT_STROKE_WIDTH)
                                    )
                                    
                                    // Extra glow for highlight
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                highlightColor.copy(alpha = 0.4f),
                                                Color.Transparent
                                            ),
                                            center = center,
                                            radius = radius * ThemeConfig.Neon.HIGHLIGHT_GLOW_RADIUS_MULT
                                        ),
                                        radius = radius * ThemeConfig.Neon.HIGHLIGHT_GLOW_RADIUS_MULT,
                                        center = center
                                    )
                                }
                            }
                            is Disc.Solid -> {
                                 // Metallic solid disc with tech pattern
                                // Base metallic gradient
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            SolidDiscGlow.copy(alpha = alpha),
                                            SolidDiscBase.copy(alpha = alpha)
                                        ),
                                        center = center,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = center
                                )
                
                                // Circuit pattern overlay
                                val patternPath = getTechPatternPath(radius)
                
                                withTransform({
                                    translate(center.x, center.y)
                                }) {
                                    drawPath(
                                        path = patternPath,
                                        color = CyanGlow.copy(alpha = 0.15f * alpha),
                                        style = Stroke(width = 1f)
                                    )
                                }

                                if (disc.cracks > 0) {
                                    val crackPath = getNeonCrackPath(disc.crackSeed, radius)
                                    val crackColor = MagentaPulse.copy(alpha = 0.9f * alpha)
                                    
                                    clipPath(
                                        Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }
                                    ) {
                                        withTransform({
                                            translate(center.x, center.y)
                                        }) {
                                            drawPath(
                                                path = crackPath,
                                                color = crackColor,
                                                style = Stroke(
                                                    width = 2.5f,
                                                    cap = StrokeCap.Square,
                                                    join = StrokeJoin.Miter
                                                )
                                            )
                                            
                                            // Glow for the crack
                                            drawPath(
                                                path = crackPath,
                                                color = crackColor.copy(alpha = 0.4f * alpha),
                                                style = Stroke(
                                                    width = 5f,
                                                    cap = StrokeCap.Round,
                                                    join = StrokeJoin.Round
                                                )
                                            )
                                        }
                                    }
                                }

                // Edge highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f * alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                
                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) Color.Red else Color.White
                    drawCircle(
                        color = highlightColor.copy(alpha = 0.3f),
                        radius = radius * ThemeConfig.Neon.HIGHLIGHT_RADIUS_MULTIPLIER,
                        center = center,
                        style = Stroke(width = ThemeConfig.Neon.HIGHLIGHT_STROKE_WIDTH)
                    )
                }
            }
        }
    }

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
        // Draw trail (motion blur effect)
        val trailCount = 4
        for (i in 0..trailCount) {
            val trailProgress = (effectiveProgress - i * 0.05f).coerceIn(0f, 1f)
             // Only draw trail if it's behind the current position
            if (trailProgress < effectiveProgress) {
                 val trailY = startY + (endY - startY) * trailProgress
                 val trailAlpha = (1f - i * (1f / trailCount)) * 0.3f
                 
                 drawDisc(
                     disc = disc,
                     center = Offset(center.x, trailY + cellSize / 2),
                     radius = radius * (1f - i * 0.05f), // Slightly smaller trails
                     textMeasurer = textMeasurer,
                     isHighlighted = false,
                     alpha = trailAlpha
                 )
            }
        }

        // Draw main disc
        drawDisc(
            disc = disc,
            center = center,
            radius = radius,
            textMeasurer = textMeasurer,
            isHighlighted = false
        )
    }

    override fun DrawScope.drawGridBackground(size: Size, cellSize: Float) {
        // Grid with glow effect
        drawRect(
            color = SpaceBlack,
            topLeft = Offset.Zero,
            size = size
        )

        // Outer glow frame
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    CyanGlow.copy(alpha = 0f),
                    CyanGlow.copy(alpha = 0.15f),
                    CyanGlow.copy(alpha = 0f)
                )
            ),
            topLeft = Offset.Zero,
            size = size,
            style = Stroke(width = 2f)
        )
        
        // Scan lines effect
        for (i in 0..GameConfig.GRID_SIZE * ThemeConfig.Neon.SCANLINE_COUNT_FACTOR) {
             val y = (i * size.height / (GameConfig.GRID_SIZE * ThemeConfig.Neon.SCANLINE_COUNT_FACTOR))
             drawLine(
                 color = ThemeConfig.Neon.SCANLINE_COLOR,
                 start = Offset(0f, y),
                 end = Offset(size.width, y),
                 strokeWidth = ThemeConfig.Neon.SCANLINE_STROKE_WIDTH
             )
        }
    }

    override fun DrawScope.drawGridLines(size: Size, cellSize: Float) {
        for (i in 0..GameConfig.GRID_SIZE) {
            val pos = i * cellSize

            // Main line
            drawLine(
                color = GridLineGlow,
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = 1.5f
            )
            drawLine(
                color = GridLineGlow,
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = 1.5f
            )

            // Glow layer
            drawLine(
                color = CyanGlow.copy(alpha = 0.1f),
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = 3f
            )
            drawLine(
                color = CyanGlow.copy(alpha = 0.1f),
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = 3f
            )
        }
    }

    override fun DrawScope.drawCellBackground(
        topLeft: Offset,
        size: Size,
        isHighlighted: Boolean,
        isHovered: Boolean,
        highlightColor: Color?
    ) {
        if (isHighlighted) {
            val color = highlightColor ?: CyanGlow
            drawRect(
                color = color.copy(alpha = 0.2f),
                topLeft = topLeft,
                size = size
            )
        } else if (isHovered) {
             drawRect(
                color = CyanGlow.copy(alpha = ThemeConfig.Neon.HOVER_ALPHA),
                topLeft = topLeft,
                size = size
            )
        }
    }

    override fun getDiscTextStyle(radius: Float, density: Density): TextStyle {
        return TextStyle(
            fontFamily = discFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = with(density) { (radius * ThemeConfig.Neon.TEXT_SCALE_FACTOR).toSp() }
        )
    }

    override fun getDiscColor(value: Int): Color {
        return discColors[value] ?: Color.Gray
    }

    override fun getBackgroundGradient(): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                SpaceBlack,
                MidnightVoid
            )
        )
    }

    override fun getSolidDiscColor(cracks: Int): Color {
        return when (cracks) {
            0 -> SolidDiscBase
            1 -> CrackedDiscCore
            else -> CyanGlow // Very damaged
        }
    }

    override fun getGridLineColor(): Color = GridLineNeon

    override fun getScoreColor(): Color = CyanGlow
    
    override fun getHighScoreColor(): Color = MagentaPulse
    
    override fun getOverlayBackgroundColor(): Color = MidnightVoid.copy(alpha = 0.9f)
    
    override fun getLabelTextColor(): Color = CyanGlow.copy(alpha = 0.7f)

    override fun getCardBackgroundColor(): Color = MidnightVoid.copy(alpha = 0.5f)

    override fun getCardBorderColor(): Color = CyanGlow.copy(alpha = 0.3f)
}
