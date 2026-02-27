package io.github.jdanders.dropcount.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.model.Disc
import androidx.compose.ui.text.TextMeasurer

class ClassicThemeRenderer : ThemeRenderer {

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
                val baseColor = getDiscColor(disc.numericValue)

                // Main disc body with radial gradient for depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor,
                            lerp(baseColor, Color.Black, 0.15f)
                        ),
                        center = center,
                        radius = radius * 1.2f
                    ),
                    radius = radius,
                    center = center,
                    alpha = alpha
                )

                // Subtle inner highlight (top-left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = ThemeConfig.Classic.INNER_HIGHLIGHT_ALPHA * alpha),
                            Color.Transparent
                        ),
                        center = center.minus(Offset(radius * 0.3f, radius * 0.3f)),
                        radius = radius * 0.6f
                    ),
                    radius = radius * 0.8f,
                    center = center.minus(Offset(radius * 0.2f, radius * 0.2f))
                )

                // Fine outer rim
                drawCircle(
                    color = Color.Black.copy(alpha = ThemeConfig.Classic.RIM_ALPHA * alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = ThemeConfig.Classic.RIM_STROKE_WIDTH)
                )

                val textLayoutResult = textMeasurer.measure(
                    text = disc.numericValue.toString(),
                    style = getDiscTextStyle(radius, this).copy(
                        color = Color.White.copy(alpha = alpha)
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        center.x - textLayoutResult.size.width / 2,
                        center.y - textLayoutResult.size.height / 2
                    )
                )

                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) {
                        Color.Red
                    } else if (highlightValue != null) {
                        getDiscColor(highlightValue)
                    } else {
                        null
                    }
                    if (highlightColor != null) {
                        drawCircle(
                            color = highlightColor,
                            radius = radius * GameConfig.HIGHLIGHT_RADIUS_MULTIPLIER,
                            center = center,
                            style = Stroke(width = GameConfig.HIGHLIGHT_STROKE_WIDTH)
                        )
                    }
                }
            }
            is Disc.Solid -> {
                val baseColor = getSolidDiscColor(disc.cracks)

                // Solid disc with subtle metallic/stone gradient
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            lerp(baseColor, Color.Black, 0.2f)
                        ),
                        start = center.minus(Offset(radius, radius)),
                        end = center.plus(Offset(radius, radius))
                    ),
                    radius = radius,
                    center = center,
                    alpha = alpha
                )

                // Add a subtle rim
                drawCircle(
                    color = Color.Black.copy(alpha = ThemeConfig.Classic.SOLID_RIM_ALPHA * alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = ThemeConfig.Classic.SOLID_RIM_STROKE_WIDTH)
                )

                if (disc.cracks > GameConfig.SOLID_DISC_INITIAL_CRACKS) {
                    drawCrackedDiscOverlay(
                        center = center,
                        radius = radius,
                        alpha = alpha,
                        seed = disc.crackSeed
                    )
                }

                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) {
                        Color.Red
                    } else if (highlightValue != null) {
                        getDiscColor(highlightValue)
                    } else {
                        null
                    }
                    if (highlightColor != null) {
                        drawCircle(
                            color = highlightColor,
                            radius = radius * GameConfig.HIGHLIGHT_RADIUS_MULTIPLIER,
                            center = center,
                            style = Stroke(width = GameConfig.HIGHLIGHT_STROKE_WIDTH)
                        )
                    }
                }
            }
        }
    }

    override fun DrawScope.drawGridBackground(size: Size, cellSize: Float) {
        // Deep background for the grid area
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF141426),
                    Color(0xFF0A0A15)
                )
            ),
            topLeft = Offset.Zero,
            size = size
        )

        // Subtle outer border for the entire grid
        drawRect(
            color = getGridLineColor().copy(alpha = ThemeConfig.Classic.GRID_OUTER_BORDER_ALPHA),
            topLeft = Offset.Zero,
            size = size,
            style = Stroke(width = ThemeConfig.Classic.GRID_OUTER_BORDER_WIDTH)
        )
    }

    override fun DrawScope.drawGridLines(size: Size, cellSize: Float) {
        for (i in 0..GameConfig.GRID_SIZE) {
            val pos = i * cellSize
            // Main grid lines with slight transparency for better blending
            drawLine(
                color = getGridLineColor().copy(alpha = ThemeConfig.Classic.GRID_LINE_ALPHA),
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = UIConfig.GRID_STROKE_WIDTH
            )
            drawLine(
                color = getGridLineColor().copy(alpha = ThemeConfig.Classic.GRID_LINE_ALPHA),
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = UIConfig.GRID_STROKE_WIDTH
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
        val baseColor = when {
            isHighlighted && highlightColor != null ->
                highlightColor.copy(alpha = GameConfig.HIGHLIGHT_ALPHA)
            isHovered -> Color.Cyan.copy(alpha = GameConfig.HOVER_ALPHA)
            else -> BackgroundDark
        }

        // Main cell background
        drawRect(
            color = baseColor,
            topLeft = topLeft,
            size = size
        )

        // Subtle inner bevel/border for the cell
        if (!isHighlighted && !isHovered) {
            val strokeWidth = 1f
            // Darker bottom/right
            drawLine(
                color = Color.Black.copy(alpha = 0.2f),
                start = Offset(topLeft.x, topLeft.y + size.height),
                end = Offset(topLeft.x + size.width, topLeft.y + size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.2f),
                start = Offset(topLeft.x + size.width, topLeft.y),
                end = Offset(topLeft.x + size.width, topLeft.y + size.height),
                strokeWidth = strokeWidth
            )
            // Lighter top/left
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = topLeft,
                end = Offset(topLeft.x + size.width, topLeft.y),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = topLeft,
                end = Offset(topLeft.x, topLeft.y + size.height),
                strokeWidth = strokeWidth
            )
        }
    }

    override fun getDiscTextStyle(radius: Float, density: Density): TextStyle {
        return TextStyle(
            color = Color.White,
            fontSize = with(density) { (radius * GameConfig.DISC_TEXT_SIZE_FRACTION).toSp() },
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.6f),
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        )
    }

    override fun getDiscColor(value: Int): Color {
        return when (value) {
            1 -> DiscColor1
            2 -> DiscColor2
            3 -> DiscColor3
            4 -> DiscColor4
            5 -> DiscColor5
            6 -> DiscColor6
            7 -> DiscColor7
            else -> Color.Gray
        }
    }

    override fun getBackgroundGradient(): Brush {
        return Brush.radialGradient(
            colors = listOf(
                BackgroundDark,
                ThemeConfig.Classic.BACKGROUND_DARKER
            ),
            center = Offset.Unspecified,
            radius = 2000f
        )
    }

    override fun getSolidDiscColor(cracks: Int): Color {
        return if (cracks == GameConfig.SOLID_DISC_INITIAL_CRACKS) {
            SolidDiscColor
        } else {
            CrackedDiscColor
        }
    }

    override fun getGridLineColor(): Color = GridLineColor

    override fun getScoreColor(): Color = ScoreColor

    override fun getHighScoreColor(): Color = AccentGold

    override fun getOverlayBackgroundColor(): Color = ThemeConfig.Overlay.CLASSIC_BACKGROUND

    override fun getLabelTextColor(): Color = Color.White.copy(alpha = 0.7f)

    override fun getCardBackgroundColor(): Color = Color(0xFF24243D)

    override fun getCardBorderColor(): Color = Color.White.copy(alpha = 0.1f)

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
        // Classic theme: No trails, just draw the disc
        drawDisc(
            disc = disc,
            center = center,
            radius = radius,
            textMeasurer = textMeasurer,
            isHighlighted = false
        )
    }

    private fun DrawScope.drawCrackedDiscOverlay(
        center: Offset,
        radius: Float,
        alpha: Float,
        seed: Int
    ) {
        val random = kotlin.random.Random(seed)
        val crackColor = Color.White.copy(alpha = alpha)

        clipPath(
            Path().apply {
                addOval(Rect(center = center, radius = radius))
            }
        ) {
            val config = ThemeConfig.Classic
            val baseAngle = config.CRACK_BASE_ANGLE
            val maxAngleSpread = Math.toRadians(config.CRACK_MAX_ANGLE_SPREAD_DEG)

            fun dirFor(angleOffset: Double): Offset =
                Offset(
                    Math.cos(baseAngle + angleOffset).toFloat(),
                    Math.sin(baseAngle + angleOffset).toFloat()
                )

            data class CrackSpec(
                val angleOffset: Double,
                val offset: Float,
                val lengthFactor: Float,
                val thickness: Float
            )

            val cracks = mutableListOf<CrackSpec>()

            // ---- thick primary cracks ----
            val thickCount = 1 + random.nextInt(3)
            repeat(thickCount) {
                cracks += CrackSpec(
                    angleOffset =
                        (random.nextDouble() * 2 - 1) * maxAngleSpread,
                    offset =
                        (random.nextFloat() - 0.5f) * radius * 0.5f,
                    lengthFactor = 1.45f,
                    thickness = radius * ThemeConfig.Classic.CRACK_THICK_FACTOR
                )
            }

            // ---- thin primary cracks ----
            val thinPrimaryCount = 1 + random.nextInt(2)
            repeat(thinPrimaryCount) {
                cracks += CrackSpec(
                    angleOffset =
                        (random.nextDouble() * 2 - 1) * maxAngleSpread,
                    offset =
                        (random.nextFloat() - 0.5f) * radius * 0.8f,
                    lengthFactor = 1.2f,
                    thickness = radius * ThemeConfig.Classic.CRACK_THIN_FACTOR
                )
            }

            val thickOffsets = cracks
                .filter { it.thickness > radius * 0.06f }
                .map { it.offset }

            // ---- partial parallel cracks ----
            val partialCount = 6 + random.nextInt(8)
            repeat(partialCount) {
                val sign =
                    if (random.nextBoolean()) 1 else -1

                val baseOffset =
                    if (thickOffsets.isNotEmpty())
                        thickOffsets[random.nextInt(thickOffsets.size)]
                    else 0f

                cracks += CrackSpec(
                    angleOffset =
                        (random.nextDouble() * 2 - 1) * maxAngleSpread,
                    offset =
                        baseOffset +
                            sign *
                            (radius * 0.25f +
                                random.nextFloat() * radius * 0.35f),
                    lengthFactor =
                        0.15f + random.nextFloat() * 0.55f,
                    thickness = radius * ThemeConfig.Classic.CRACK_PARTIAL_FACTOR
                )
            }

            // ---- draw ----
            cracks.forEach { spec ->
                val dir = dirFor(spec.angleOffset)
                val normal = Offset(-dir.y, dir.x)

                val halfLength = radius * spec.lengthFactor
                val start =
                    center - dir * halfLength + normal * spec.offset
                val end =
                    center + dir * halfLength + normal * spec.offset

                val totalLength = (end - start).getDistance()

                val path = fracturedPath(
                    start = start,
                    direction = dir,
                    normal = normal,
                    totalLength = totalLength,
                    radius = radius,
                    random = random
                )

                drawPath(
                    path = path,
                    color = crackColor,
                    style = Stroke(
                        width = spec.thickness,
                        cap = StrokeCap.Butt,
                        join = StrokeJoin.Miter
                    )
                )
            }
        }
    }

    private fun fracturedPath(
        start: Offset,
        direction: Offset,
        normal: Offset,
        totalLength: Float,
        radius: Float,
        random: kotlin.random.Random
    ): Path {
        val path = Path()
        path.moveTo(start.x, start.y)

        var current = start
        var remaining = totalLength

        while (remaining > 0f) {
            val config = ThemeConfig.Classic
            val segment =
                radius * (config.CRACK_SEGMENT_BASE + random.nextFloat() * config.CRACK_SEGMENT_VARIANCE)

            val jitterAngle =
                (random.nextFloat() - 0.5f) * config.CRACK_JITTER_FACTOR

            val dir =
                (direction.rotate(jitterAngle)).normalize()

            val step = dir * segment +
                normal * (random.nextFloat() - 0.5f) * radius * 0.03f

            val next = current + step

            path.lineTo(next.x, next.y)

            current = next
            remaining -= segment
        }

        return path
    }

    private fun Offset.rotate(angleRadians: Float): Offset {
        val cos = kotlin.math.cos(angleRadians)
        val sin = kotlin.math.sin(angleRadians)
        return Offset(
            x * cos - y * sin,
            x * sin + y * cos
        )
    }

    private fun Offset.normalize(): Offset {
        val length = kotlin.math.hypot(x, y)
        return if (length == 0f) this else Offset(x / length, y / length)
    }
}
