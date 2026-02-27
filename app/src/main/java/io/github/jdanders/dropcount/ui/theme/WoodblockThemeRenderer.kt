package io.github.jdanders.dropcount.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.model.Disc
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Woodblock theme: Hanko seal stamps with calligraphic numbers
 * - Square stamps with slightly rounded corners (like traditional Japanese seals)
 * - Rich vermilion red and other seal colors
 * - WHITE calligraphic numbers with strong shadows for visibility
 * - Subtle paper fiber texture overlay
 * - Stamped impression effect
 */
class WoodblockThemeRenderer : ThemeRenderer {

    private val hankoPathCache = mutableMapOf<Int, Path>()
    private val kintsugiPathCache = mutableMapOf<Int, Path>()
    private var lastCachedRadius = 0f

    // Background texture cache
    private var grainPositions: List<Offset>? = null
    private var longFiberLines: List<Triple<Offset, Offset, Float>>? = null // start, end, thickness
    private var shortFiberLines: List<Pair<Offset, Offset>>? = null
    private var lastGridSize: Size? = null

    private fun updateBackgroundCache(size: Size) {
        if (lastGridSize == size && grainPositions != null) return

        lastGridSize = size
        val random = Random(42)

        grainPositions = List(ThemeConfig.Woodblock.GRAIN_COUNT) {
            Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
        }

        longFiberLines = List(ThemeConfig.Woodblock.LONG_FIBER_COUNT) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val length = 15f + random.nextFloat() * 35f
            val angle = random.nextFloat() * 360f
            val thickness = 0.8f + random.nextFloat() * 1.2f
            val endX = x + length * cos(Math.toRadians(angle.toDouble())).toFloat()
            val endY = y + length * sin(Math.toRadians(angle.toDouble())).toFloat()
            Triple(Offset(x, y), Offset(endX, endY), thickness)
        }

        shortFiberLines = List(ThemeConfig.Woodblock.SHORT_FIBER_COUNT) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val length = ThemeConfig.Woodblock.PAPER_FIBER_MIN_LENGTH + random.nextFloat() * ThemeConfig.Woodblock.PAPER_FIBER_MAX_LENGTH
            val angle = random.nextFloat() * 360f
            val endX = x + length * cos(Math.toRadians(angle.toDouble())).toFloat()
            val endY = y + length * sin(Math.toRadians(angle.toDouble())).toFloat()
            Pair(Offset(x, y), Offset(endX, endY))
        }
    }

    private fun getHankoPath(seed: Int, radius: Float): Path {
        val sealSize = radius * ThemeConfig.Woodblock.SEAL_SIZE_MULTIPLIER
        if (lastCachedRadius != radius) {
            hankoPathCache.clear()
            kintsugiPathCache.clear()
            lastCachedRadius = radius
        }
        return hankoPathCache.getOrPut(seed) {
            val rect = Rect(
                left = -sealSize / 2,
                top = -sealSize / 2,
                right = sealSize / 2,
                bottom = sealSize / 2
            )
            createHankoPath(rect, seed)
        }
    }

    private fun getKintsugiPath(seed: Int, radius: Float): Path {
        val sealSize = radius * ThemeConfig.Woodblock.SEAL_SIZE_MULTIPLIER
        if (lastCachedRadius != radius) {
            hankoPathCache.clear()
            kintsugiPathCache.clear()
            lastCachedRadius = radius
        }
        return kintsugiPathCache.getOrPut(seed) {
            generateKintsugiPath(seed, sealSize)
        }
    }

    private fun generateKintsugiPath(seed: Int, sealSize: Float): Path {
        val random = Random(seed)
        val fullPath = Path()
        val actualCrackCount = 2 + random.nextInt(3)

        repeat(actualCrackCount) {
            val startAngle = random.nextFloat() * 360f
            val startRadius = sealSize * 0.45f
            var x = startRadius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
            var y = startRadius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
            fullPath.moveTo(x, y)

            var currentAngle = startAngle + 180f + (random.nextFloat() - 0.5f) * 30f
            repeat(ThemeConfig.Woodblock.CRACK_SEGMENTS) {
                currentAngle += (random.nextFloat() - 0.5f) * ThemeConfig.Woodblock.CRACK_ANGLE_VARIATION
                val dist = sealSize * (0.1f + random.nextFloat() * ThemeConfig.Woodblock.CRACK_SEGMENT_LENGTH)
                x += dist * cos(Math.toRadians(currentAngle.toDouble())).toFloat()
                y += dist * sin(Math.toRadians(currentAngle.toDouble())).toFloat()
                fullPath.lineTo(x, y)
            }
        }
        return fullPath
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
        when (disc) {
            is Disc.Numbered -> {
                val value = disc.value
                val baseColor = getDiscColor(value)

                // Hanko seal dimensions (slightly wider square)
                val sealSize = radius * ThemeConfig.Woodblock.SEAL_SIZE_MULTIPLIER
                val sealPath = getHankoPath(value, radius)
                val sealRect = Rect(
                    left = center.x - sealSize / 2,
                    top = center.y - sealSize / 2,
                    right = center.x + sealSize / 2,
                    bottom = center.y + sealSize / 2
                )

                // Shadow (stamped impression offset)
                withTransform({
                    translate(center.x + ThemeConfig.Woodblock.SHADOW_OFFSET_X, center.y + ThemeConfig.Woodblock.SHADOW_OFFSET_Y)
                }) {
                    drawPath(
                        path = sealPath,
                        color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.SHADOW_ALPHA * alpha)
                    )
                }

                // Main seal color with subtle texture variation
                withTransform({
                    translate(center.x, center.y)
                }) {
                    drawPath(
                        path = sealPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = alpha),
                                baseColor.copy(alpha = alpha * 0.85f),
                                baseColor.copy(alpha = alpha * 0.95f)
                            ),
                            center = Offset.Zero,
                            radius = sealSize * 0.8f
                        )
                    )
                }

                // Paper fiber texture overlay
                drawPaperTexture(
                    center = center,
                    clipPath = sealPath,
                    alpha = alpha,
                    seed = value,
                    sealSize = sealSize
                )

                // Crisp border (seal edge)
                withTransform({
                    translate(center.x, center.y)
                }) {
                    drawPath(
                        path = sealPath,
                        color = WoodblockInk.copy(alpha = alpha),
                        style = Stroke(
                            width = ThemeConfig.Woodblock.OUTLINE_WIDTH,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Calligraphic number with strong visibility and brush-like appearance
                val textStyle = getDiscTextStyle(radius, this).copy(
                    color = Color.White.copy(alpha = alpha),
                    fontWeight = FontWeight.Black
                )

                val textLayoutResult = textMeasurer.measure(
                    text = value.toString(),
                    style = textStyle
                )

                val textPos = Offset(
                    center.x - textLayoutResult.size.width / 2,
                    center.y - textLayoutResult.size.height / 2
                )

                // Brush stroke base (thick dark background for calligraphic effect)
                val brushSteps = ThemeConfig.Woodblock.BRUSH_STEPS
                for (i in brushSteps downTo 1) {
                    val brushAlpha = (ThemeConfig.Woodblock.BRUSH_ALPHA_FACTOR * i / brushSteps) * alpha
                    val brushSize = ThemeConfig.Woodblock.BRUSH_SIZE_FACTOR * i

                    // Asymmetric brush stroke pattern (like brush pressure)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textPos + Offset(-brushSize * 0.5f, brushSize),
                        color = WoodblockInk.copy(alpha = brushAlpha)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textPos + Offset(brushSize * 0.5f, -brushSize * 0.3f),
                        color = WoodblockInk.copy(alpha = brushAlpha * 0.7f)
                    )
                }

                // Strong dark outline for visibility (3-layer shadow)
                val outlineSteps = ThemeConfig.Woodblock.NUMBER_OUTLINE_STEPS
                for (i in outlineSteps downTo 1) {
                    val outlineAlpha = (ThemeConfig.Woodblock.NUMBER_OUTLINE_ALPHA * (outlineSteps - i + 1) / outlineSteps) * alpha
                    val outlineSize = ThemeConfig.Woodblock.NUMBER_OUTLINE_SIZE * i

                    // 8 directions
                    for (angle in 0 until 360 step 45) {
                        val rad = Math.toRadians(angle.toDouble())
                        val offsetX = (cos(rad) * outlineSize).toFloat()
                        val offsetY = (sin(rad) * outlineSize).toFloat()

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = textPos + Offset(offsetX, offsetY),
                            color = WoodblockInk.copy(alpha = outlineAlpha)
                        )
                    }
                }

                // Subtle colored glow for depth
                val glowSteps = ThemeConfig.Woodblock.NUMBER_GLOW_STEPS
                for (i in glowSteps downTo 1) {
                    val glowAlpha = (ThemeConfig.Woodblock.NUMBER_GLOW_ALPHA * (glowSteps - i + 1) / glowSteps) * alpha
                    val glowSize = ThemeConfig.Woodblock.NUMBER_GLOW_SIZE * i

                    for (angle in 0 until 360 step 45) {
                        val rad = Math.toRadians(angle.toDouble())
                        val offsetX = (cos(rad) * glowSize).toFloat()
                        val offsetY = (sin(rad) * glowSize).toFloat()

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = textPos + Offset(offsetX, offsetY),
                            color = baseColor.copy(alpha = glowAlpha)
                        )
                    }
                }

                // Main WHITE number on top (crisp and bold)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textPos
                )

                // Highlight
                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) {
                        WoodblockVermilion
                    } else if (highlightValue != null) {
                        getDiscColor(highlightValue)
                    } else {
                        WoodblockGold
                    }

                    val highlightRect = Rect(
                        left = sealRect.left - ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        top = sealRect.top - ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        right = sealRect.right + ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        bottom = sealRect.bottom + ThemeConfig.Woodblock.HIGHLIGHT_PADDING
                    )
                    val highlightPath = createHankoPath(highlightRect, value)

                    drawPath(
                        path = highlightPath,
                        color = highlightColor.copy(alpha = ThemeConfig.Woodblock.HIGHLIGHT_ALPHA),
                        style = Stroke(
                            width = ThemeConfig.Woodblock.HIGHLIGHT_STROKE_WIDTH,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
            is Disc.Solid -> {
                // Hanko seal for solid discs - natural wood/stone color
                val sealSize = radius * ThemeConfig.Woodblock.SEAL_SIZE_MULTIPLIER
                val sealPath = getHankoPath(disc.crackSeed, radius)
                val sealRect = Rect(
                    left = center.x - sealSize / 2,
                    top = center.y - sealSize / 2,
                    right = center.x + sealSize / 2,
                    bottom = center.y + sealSize / 2
                )

                // Shadow
                withTransform({
                    translate(center.x + ThemeConfig.Woodblock.SHADOW_OFFSET_X, center.y + ThemeConfig.Woodblock.SHADOW_OFFSET_Y)
                }) {
                    drawPath(
                        path = sealPath,
                        color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.SHADOW_ALPHA * alpha)
                    )
                }

                // Natural stone/wood color
                val baseColor = getSolidDiscColor(disc.cracks)
                withTransform({
                    translate(center.x, center.y)
                }) {
                    drawPath(
                        path = sealPath,
                        color = baseColor.copy(alpha = alpha)
                    )
                }

                // Paper texture overlay
                drawPaperTexture(
                    center = center,
                    clipPath = sealPath,
                    alpha = alpha * ThemeConfig.Woodblock.SOLID_GRAIN_INTENSITY,
                    seed = disc.crackSeed,
                    sealSize = sealSize
                )

                // Cracks as gold kintsugi lines
                if (disc.cracks > 0) {
                    val kintsugiPath = getKintsugiPath(disc.crackSeed, radius)

                    clipPath(sealPath) {
                        withTransform({
                            translate(center.x, center.y)
                        }) {
                            // Dark outline
                            drawPath(
                                path = kintsugiPath,
                                color = WoodblockInk.copy(alpha = 0.7f * alpha),
                                style = Stroke(width = ThemeConfig.Woodblock.CRACK_OUTLINE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Bright gold fill
                            drawPath(
                                path = kintsugiPath,
                                color = WoodblockGold.copy(alpha = alpha),
                                style = Stroke(width = ThemeConfig.Woodblock.CRACK_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Cancellation stamp: bold vermilion × stamped over the broken seal
                    drawCancellationStamp(
                        center = center,
                        sealSize = sealSize,
                        alpha = alpha,
                        seed = disc.crackSeed
                    )
                }

                // Border
                withTransform({
                    translate(center.x, center.y)
                }) {
                    drawPath(
                        path = sealPath,
                        color = WoodblockInk.copy(alpha = alpha),
                        style = Stroke(
                            width = ThemeConfig.Woodblock.OUTLINE_WIDTH,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Highlight
                if (isHighlighted) {
                    val highlightColor = if (isGameOverHighlight) {
                        WoodblockVermilion
                    } else if (highlightValue != null) {
                        getDiscColor(highlightValue)
                    } else {
                        WoodblockGold
                    }

                    val highlightRect = Rect(
                        left = sealRect.left - ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        top = sealRect.top - ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        right = sealRect.right + ThemeConfig.Woodblock.HIGHLIGHT_PADDING,
                        bottom = sealRect.bottom + ThemeConfig.Woodblock.HIGHLIGHT_PADDING
                    )
                    val highlightPath = createHankoPath(highlightRect, disc.crackSeed)

                    drawPath(
                        path = highlightPath,
                        color = highlightColor.copy(alpha = ThemeConfig.Woodblock.HIGHLIGHT_ALPHA),
                        style = Stroke(
                            width = ThemeConfig.Woodblock.HIGHLIGHT_STROKE_WIDTH,
                            cap = StrokeCap.Round
                        )
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
        // Simple drop with slight rotation
        val rotation = effectiveProgress * 15f

        drawContext.canvas.save()
        drawContext.canvas.translate(center.x, center.y)
        drawContext.canvas.rotate(rotation)
        drawContext.canvas.translate(-center.x, -center.y)

        drawDisc(
            disc = disc,
            center = center,
            radius = radius,
            textMeasurer = textMeasurer,
            isHighlighted = false,
            alpha = 1f
        )

        drawContext.canvas.restore()
    }

    override fun DrawScope.drawGridBackground(size: Size, cellSize: Float) {
        // Warm cream paper background
        drawRect(
            color = WoodblockPaper,
            topLeft = Offset.Zero,
            size = size
        )

        updateBackgroundCache(size)

        // Paper grain (dots for texture "tooth")
        grainPositions?.forEach { pos ->
            drawCircle(
                color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.GRAIN_ALPHA),
                radius = ThemeConfig.Woodblock.GRAIN_SIZE,
                center = pos
            )
        }

        // Long fibers (like washi paper)
        longFiberLines?.forEach { (start, end, thickness) ->
            drawLine(
                color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.PAPER_FIBER_ALPHA),
                start = start,
                end = end,
                strokeWidth = thickness,
                cap = StrokeCap.Round
            )
        }

        // Short fibers
        shortFiberLines?.forEach { (start, end) ->
            drawLine(
                color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.PAPER_FIBER_ALPHA * 0.6f),
                start = start,
                end = end,
                strokeWidth = 0.6f,
                cap = StrokeCap.Round
            )
        }

        // Frame
        drawRect(
            color = WoodblockInk,
            topLeft = Offset.Zero,
            size = size,
            style = Stroke(width = 4f)
        )

        // Corner seal (artist's hanko)
        val sealSize = 28f
        val margin = 16f
        drawCircle(
            color = WoodblockVermilion,
            radius = sealSize / 2,
            center = Offset(margin + sealSize / 2, margin + sealSize / 2)
        )
        drawCircle(
            color = WoodblockPaper,
            radius = sealSize * 0.25f,
            center = Offset(margin + sealSize / 2, margin + sealSize / 2)
        )
    }

    override fun DrawScope.drawGridLines(size: Size, cellSize: Float) {
        val cols = (size.width / cellSize).toInt()
        val rows = (size.height / cellSize).toInt()

        // Clean grid lines with slight hand-drawn wobble
        val random = Random(123)

        for (i in 0..cols) {
            val x = i * cellSize
            val wobble = (random.nextFloat() - 0.5f) * ThemeConfig.Woodblock.GRID_WOBBLE_FACTOR
            drawLine(
                color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.GRID_LINE_ALPHA),
                start = Offset(x + wobble, 0f),
                end = Offset(x + wobble, size.height),
                strokeWidth = ThemeConfig.Woodblock.GRID_LINE_WIDTH,
                cap = StrokeCap.Round
            )
        }

        for (i in 0..rows) {
            val y = i * cellSize
            val wobble = (random.nextFloat() - 0.5f) * ThemeConfig.Woodblock.GRID_WOBBLE_FACTOR
            drawLine(
                color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.GRID_LINE_ALPHA),
                start = Offset(0f, y + wobble),
                end = Offset(size.width, y + wobble),
                strokeWidth = ThemeConfig.Woodblock.GRID_LINE_WIDTH,
                cap = StrokeCap.Round
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
            val baseHighlightColor = highlightColor ?: WoodblockGold
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseHighlightColor.copy(alpha = 0.3f),
                        baseHighlightColor.copy(alpha = 0.15f)
                    ),
                    center = topLeft + Offset(size.width / 2, size.height / 2),
                    radius = size.width * 0.7f
                ),
                topLeft = topLeft,
                size = size
            )
        } else if (isHovered) {
            drawRect(
                color = WoodblockIndigo.copy(alpha = 0.12f),
                topLeft = topLeft,
                size = size
            )
        }
    }

    override fun getDiscTextStyle(radius: Float, density: Density): TextStyle {
        return TextStyle(
            fontFamily = FontFamily(
                Font(R.font.waterbrush, FontWeight.Bold)
            ),
            fontWeight = FontWeight.Bold,
            fontSize = with(density) { (radius * 1.6f).toSp() },
            letterSpacing = (-0.05).em
        )
    }

    override fun getDiscColor(value: Int): Color {
        return when (value) {
            1 -> WoodblockVermilion
            2 -> WoodblockIndigo
            3 -> WoodblockGold
            4 -> WoodblockSage
            5 -> WoodblockPlum
            6 -> WoodblockTeal
            7 -> WoodblockCoral
            else -> WoodblockInk
        }
    }

    override fun getBackgroundGradient(): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                WoodblockPaper,
                WoodblockPaper.copy(alpha = 0.95f)
            )
        )
    }

    override fun getSolidDiscColor(cracks: Int): Color {
        return when (cracks) {
            0 -> WoodblockWood
            1 -> WoodblockWoodLight
            else -> WoodblockWoodLightest
        }
    }

    override fun getGridLineColor(): Color = WoodblockInk
    override fun getScoreColor(): Color = WoodblockVermilion
    override fun getHighScoreColor(): Color = WoodblockGold
    override fun getOverlayBackgroundColor(): Color =
        WoodblockPaper.copy(alpha = 0.92f)
    override fun getLabelTextColor(): Color = WoodblockInk

    override fun getCardBackgroundColor(): Color = Color.Transparent

    override fun getCardBorderColor(): Color = WoodblockInk

    // Helper: Create hanko seal path (rounded square)
    private fun createHankoPath(rect: Rect, seed: Int): Path {
        val path = Path()
        val random = Random(seed)
        val cornerRadius = rect.width * ThemeConfig.Woodblock.SEAL_CORNER_RADIUS_FACTOR // Slightly rounded corners

        // Add slight irregularity to edges (hand-carved effect)
        val wobbleAmount = rect.width * ThemeConfig.Woodblock.SEAL_EDGE_WOBBLE_FACTOR

        // Top edge
        path.moveTo(rect.left + cornerRadius, rect.top + random.nextFloat() * wobbleAmount)
        path.lineTo(rect.right - cornerRadius, rect.top + random.nextFloat() * wobbleAmount)

        // Top-right corner
        path.quadraticTo(
            rect.right - random.nextFloat() * wobbleAmount,
            rect.top + random.nextFloat() * wobbleAmount,
            rect.right - random.nextFloat() * wobbleAmount,
            rect.top + cornerRadius
        )

        // Right edge
        path.lineTo(rect.right - random.nextFloat() * wobbleAmount, rect.bottom - cornerRadius)

        // Bottom-right corner
        path.quadraticTo(
            rect.right - random.nextFloat() * wobbleAmount,
            rect.bottom - random.nextFloat() * wobbleAmount,
            rect.right - cornerRadius,
            rect.bottom - random.nextFloat() * wobbleAmount
        )

        // Bottom edge
        path.lineTo(rect.left + cornerRadius, rect.bottom - random.nextFloat() * wobbleAmount)

        // Bottom-left corner
        path.quadraticTo(
            rect.left + random.nextFloat() * wobbleAmount,
            rect.bottom - random.nextFloat() * wobbleAmount,
            rect.left + random.nextFloat() * wobbleAmount,
            rect.bottom - cornerRadius
        )

        // Left edge
        path.lineTo(rect.left + random.nextFloat() * wobbleAmount, rect.top + cornerRadius)

        // Top-left corner
        path.quadraticTo(
            rect.left + random.nextFloat() * wobbleAmount,
            rect.top + random.nextFloat() * wobbleAmount,
            rect.left + cornerRadius,
            rect.top + random.nextFloat() * wobbleAmount
        )

        path.close()
        return path
    }

    /**
     * Draws a bold vermilion cancellation stamp (消印 / kesimushi) over a shattered solid disc.
     *
     * Each arm of the × is rendered as a calligraphic brush stroke: a filled Path whose
     * width swells toward the middle and tapers at both ends, with a slight inward curve
     * (controlled by a quadratic Bezier control point offset perpendicular to the stroke).
     * This mimics the uneven pressure of a broad brush loaded with ink — thick where the
     * brush contacts, thin where it lifts — giving the mark a hand-painted kanji quality
     * rather than a mechanical line.
     *
     * The stamp is rotated slightly (seeded, so consistent per disc) around the disc center
     * to look freshly slapped on by hand.
     */
    private fun DrawScope.drawCancellationStamp(
        center: Offset,
        sealSize: Float,
        alpha: Float,
        seed: Int
    ) {
        val random = Random(seed + 999)
        val extent = sealSize * ThemeConfig.Woodblock.CANCEL_STAMP_EXTENT_FACTOR
        val maxWidth = sealSize * ThemeConfig.Woodblock.CANCEL_STAMP_STROKE_FACTOR
        val rotation = (random.nextFloat() - 0.5f) * 2f * ThemeConfig.Woodblock.CANCEL_STAMP_ROTATION_RANGE

        // Rotate around the disc center (pivot = center keeps it centered)
        withTransform({ rotate(degrees = rotation, pivot = center) }) {

            // Each arm: defined in canvas coordinates relative to center
            data class Arm(val from: Offset, val to: Offset, val curveBias: Float)
            val arms = listOf(
                // top-left → bottom-right: curve bows slightly upward (↗ perpendicular)
                Arm(
                    from = center + Offset(-extent, -extent),
                    to   = center + Offset( extent,  extent),
                    curveBias = -0.08f   // negative = curves toward top-right
                ),
                // top-right → bottom-left: curve bows slightly downward (↙ perpendicular)
                Arm(
                    from = center + Offset( extent, -extent),
                    to   = center + Offset(-extent,  extent),
                    curveBias =  0.08f
                )
            )

            arms.forEach { arm ->
                val dx = arm.to.x - arm.from.x
                val dy = arm.to.y - arm.from.y
                val len = kotlin.math.sqrt(dx * dx + dy * dy)

                // Unit vectors along and perpendicular to the stroke
                val ux = dx / len
                val uy = dy / len
                val px = -uy   // perpendicular
                val py =  ux

                // Midpoint with a slight perpendicular curve bias (kanji brush arc)
                val mid = Offset(
                    (arm.from.x + arm.to.x) / 2f + px * len * arm.curveBias,
                    (arm.from.y + arm.to.y) / 2f + py * len * arm.curveBias
                )

                // Build a filled outline path for the stroke.
                // We walk from tip→tip along the "top" edge then back along the "bottom" edge,
                // using the perpendicular to extrude the half-width at each sample point.
                // Width profile: 0 at tip → maxWidth at centre → 0 at tip (smooth bell).
                val steps = ThemeConfig.Woodblock.CANCEL_STAMP_CURVE_STEPS
                val path = Path()

                // Collect top-edge points (from → to, offset by +halfWidth perpendicular)
                val topEdge = mutableListOf<Offset>()
                val botEdge = mutableListOf<Offset>()

                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    // Quadratic Bezier position: B(t) = (1-t)²·P0 + 2(1-t)t·P1 + t²·P2
                    val s = 1f - t
                    val bx = s * s * arm.from.x + 2f * s * t * mid.x + t * t * arm.to.x
                    val by = s * s * arm.from.y + 2f * s * t * mid.y + t * t * arm.to.y

                    // Quadratic Bezier tangent direction (derivative)
                    val tdx = 2f * (s * (mid.x - arm.from.x) + t * (arm.to.x - mid.x))
                    val tdy = 2f * (s * (mid.y - arm.from.y) + t * (arm.to.y - mid.y))
                    val tlen = kotlin.math.sqrt(tdx * tdx + tdy * tdy).coerceAtLeast(0.001f)
                    val npx = -tdy / tlen  // perpendicular to tangent
                    val npy =  tdx / tlen

                    // Bell-curve width profile: sin(π·t) peaks at midpoint
                    val widthT = kotlin.math.sin(Math.PI.toFloat() * t).coerceIn(0f, 1f)
                    // Extra calligraphic twist: the two arms have slightly different profiles
                    val halfWidth = maxWidth * 0.5f * widthT *
                            (1f + arm.curveBias * 2f * (t - 0.5f))  // subtle asymmetry

                    topEdge.add(Offset(bx + npx * halfWidth, by + npy * halfWidth))
                    botEdge.add(Offset(bx - npx * halfWidth, by - npy * halfWidth))
                }

                // Build path: top edge forward, bottom edge reversed
                topEdge.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                botEdge.reversed().forEach { pt -> path.lineTo(pt.x, pt.y) }
                path.close()

                // Ink shadow (dark outline for stamp depth)
                drawPath(
                    path = path,
                    color = WoodblockInk.copy(alpha = 0.25f * alpha)
                )
                // Main vermilion fill
                drawPath(
                    path = path,
                    color = WoodblockVermilion.copy(alpha = ThemeConfig.Woodblock.CANCEL_STAMP_ALPHA * alpha)
                )
                // Bright highlight along the top edge (wet ink sheen)
                drawPath(
                    path = path,
                    color = WoodblockVermilion.copy(red = 1f, alpha = 0.35f * alpha),
                    style = Stroke(width = maxWidth * 0.12f, cap = StrokeCap.Round)
                )
            }

            // Ink-splatter dots at the four stroke tips
            val splatRadius = maxWidth * ThemeConfig.Woodblock.CANCEL_STAMP_SPLAT_RADIUS_FACTOR
            arms.flatMap { listOf(it.from, it.to) }.forEach { tip ->
                val r = splatRadius * (0.6f + random.nextFloat() * 0.8f)
                drawCircle(
                    color = WoodblockVermilion.copy(alpha = ThemeConfig.Woodblock.CANCEL_STAMP_SPLAT_ALPHA * alpha),
                    radius = r,
                    center = tip
                )
                // Small satellite splat
                val ox = (random.nextFloat() - 0.5f) * splatRadius * 2.5f
                val oy = (random.nextFloat() - 0.5f) * splatRadius * 2.5f
                drawCircle(
                    color = WoodblockVermilion.copy(alpha = ThemeConfig.Woodblock.CANCEL_STAMP_SPLAT_ALPHA * 0.5f * alpha),
                    radius = r * 0.45f,
                    center = tip + Offset(ox, oy)
                )
            }
        }
    }

    private val discTextureCache = mutableMapOf<Int, List<Pair<Offset, Offset>>>()

    // Helper: Draw paper fiber texture
    private fun DrawScope.drawPaperTexture(
        center: Offset,
        clipPath: Path,
        alpha: Float,
        seed: Int,
        sealSize: Float
    ) {
        if (lastCachedRadius != sealSize / ThemeConfig.Woodblock.SEAL_SIZE_MULTIPLIER) {
            discTextureCache.clear()
        }

        val fibers = discTextureCache.getOrPut(seed) {
            val random = Random(seed)
            val fiberCount = ((sealSize * sealSize) / 150f).toInt()
            List(fiberCount) {
                val x = (random.nextFloat() - 0.5f) * sealSize
                val y = (random.nextFloat() - 0.5f) * sealSize
                val length = 2f + random.nextFloat() * 8f
                val angle = random.nextFloat() * 360f
                val endX = x + length * cos(Math.toRadians(angle.toDouble())).toFloat()
                val endY = y + length * sin(Math.toRadians(angle.toDouble())).toFloat()
                Pair(Offset(x, y), Offset(endX, endY))
            }
        }

        clipPath(clipPath) {
            withTransform({
                translate(center.x, center.y)
            }) {
                fibers.forEach { (start, end) ->
                    drawLine(
                        color = WoodblockInk.copy(alpha = ThemeConfig.Woodblock.PAPER_FIBER_ALPHA * alpha),
                        start = start,
                        end = end,
                        strokeWidth = 0.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}