package io.github.jdanders.dropcount.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R
import io.github.jdanders.dropcount.config.AnimationConfig
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.model.*
import io.github.jdanders.dropcount.ui.theme.*
import io.github.jdanders.dropcount.util.Logger

@Composable
fun GameGrid(
    gameState: GameState,
    onColumnTap: (Int) -> Unit,
    animationState: AnimationState,
    hoveredColumn: Int?,
    onHoveredColumnChange: (Int?) -> Unit,
    floatingPoints: Map<GridPosition, Int>,
    animationSpeed: AnimationSpeed,
    visualTheme: VisualTheme,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val themeRenderer = remember(visualTheme) { visualTheme.createRenderer() }

    // Animation progress for current animation state
    val animationProgress = remember { Animatable(0f) }

    // Track previous animation state to detect transitions
    var previousAnimationState by remember { mutableStateOf<AnimationState>(AnimationState.Idle) }

    // Trigger animation when state changes
    LaunchedEffect(animationState) {
        if (animationState != previousAnimationState) {
            Logger.d("GameGrid", "Animation state changed from $previousAnimationState to $animationState")
            previousAnimationState = animationState

            when (animationState) {
                is AnimationState.DroppingDisc -> {
                    Logger.d("GameGrid", "Starting DroppingDisc animation, resetting progress")
                    animationProgress.snapTo(0f)
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AnimationConfig.getDropDuration(animationSpeed).toInt(),
                            easing = EaseIn
                        )
                    )
                }
                is AnimationState.HighlightingMatches -> {
                    Logger.d("GameGrid", "Starting HighlightingMatches animation, resetting progress")
                    animationProgress.snapTo(0f)
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AnimationConfig.getHighlightDuration(animationSpeed).toInt()
                        )
                    )
                }
                is AnimationState.ApplyingGravity -> {
                    Logger.d("GameGrid", "Starting ApplyingGravity animation, resetting progress")
                    animationProgress.snapTo(0f)
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AnimationConfig.getGravityDuration(animationSpeed, animationState.movements.isNotEmpty()).toInt(),
                            easing = EaseIn
                        )
                    )
                }
                is AnimationState.AddingNewRow -> {
                    Logger.d("GameGrid", "Starting AddingNewRow animation, current progress=${animationProgress.value}, resetting to 0")
                    animationProgress.snapTo(0f)
                    Logger.d("GameGrid", "Progress after reset=${animationProgress.value}")
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AnimationConfig.getNewRowDisplayDuration(animationSpeed).toInt(),
                            easing = EaseIn
                        )
                    )
                    Logger.d("GameGrid", "AddingNewRow animation complete")
                }
                AnimationState.Idle -> {
                    Logger.d("GameGrid", "Entering Idle state, resetting progress")
                    animationProgress.snapTo(0f)
                }
            }
        }
    }

    var canvasSize by remember { mutableStateOf(0f) }

    // Clear hover when animations start
    LaunchedEffect(animationState) {
        if (animationState !is AnimationState.Idle) {
            onHoveredColumnChange(null)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Provide space for preview disc and game-over discs drawn above the grid
        Box(modifier = Modifier.fillMaxWidth()) {
            // We'll calculate cellSize here from the actual size if possible, 
            // but for now we'll use a simpler approach since we expect to be 
            // in a BoxWithConstraints or have a fixed size.
            // Using a Canvas-like approach or just a Spacer with weight might be better,
            // but let's keep it simple for now and rely on the aspectRatio of the Canvas below.
            
            // To make the top spacer match the cellSize of the grid below:
            // The grid is fillMaxWidth(), so cellSize = width / 7.
            // Spacer height should be 2 * cellSize = width * 2 / 7.
            // We can use aspectRatio(7/2f) for this!
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(GameConfig.GRID_SIZE / 2.0f)
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val nextDiscType = when (val disc = gameState.nextDisc) {
                is Disc.Numbered -> stringResource(R.string.acc_disc_numbered, disc.value)
                is Disc.Solid -> stringResource(R.string.acc_disc_solid)
            }
            val previewDescription = stringResource(R.string.acc_preview_disc, nextDiscType)

            val gridDescription = stringResource(
                R.string.acc_grid_description,
                gameState.level,
                gameState.score,
                gameState.grid.flatten().count { it.isOccupied() }
            ) + " " + previewDescription

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onGloballyPositioned { coords ->
                        canvasOffset = coords.positionInParent()
                        canvasSize = coords.size.width.toFloat()
                    }
                    .semantics {
                        contentDescription = gridDescription
                    }
            ) {
                canvasSize = size.width
                val cellSize = size.width / GameState.GRID_SIZE

                // CRITICAL: Use the correct progress for the current animation state
                // If we just entered a new animation state, the LaunchedEffect hasn't run yet
                // so we need to treat progress as 0 for new animations
                val effectiveProgress = if (animationState != previousAnimationState) {
                    0f // Just transitioned, use 0 even if Animatable hasn't reset yet
                } else {
                    animationProgress.value
                }

                // Determine what state to draw based on animation
                val stateToRender = when (val anim = animationState) {
                    is AnimationState.AddingNewRow -> {
                        // Use embedded state during animation, not gameState
                        // This prevents flashing when gameState updates mid-animation
                        Logger.d("GameGrid", "AddingNewRow: using stateBeforeNewRow, progress=$effectiveProgress")
                        anim.stateBeforeNewRow
                    }
                    else -> {
                        if (animationState is AnimationState.Idle) {
                            Logger.d("GameGrid", "Idle: using gameState")
                        }
                        gameState
                    }
                }

                val highlightedPositions = (animationState as? AnimationState.HighlightingMatches)?.positions ?: emptySet()
                val highlightColors = (animationState as? AnimationState.HighlightingMatches)?.colors ?: emptyMap()

                // Draw grid background
                with(themeRenderer) {
                    drawGridBackground(size, cellSize)
                }

                // Draw cells and discs
                for (row in 0 until GameConfig.GRID_SIZE) {
                    for (col in 0 until GameConfig.GRID_SIZE) {
                        val x = col * cellSize
                        val y = row * cellSize

                        val cell = stateToRender.getCell(row, col)
                        val pos = GridPosition(Row(row), Col(col))
                        val isHighlighted = highlightedPositions.contains(pos)
                        val isHoveredCol = hoveredColumn == col

                        // Draw cell background
                        val highlightValue = if (isHighlighted) highlightColors[pos] else null
                        val highlightColor = if (highlightValue != null) themeRenderer.getDiscColor(highlightValue) else null
                        
                        with(themeRenderer) {
                            drawCellBackground(
                                topLeft = Offset(x + UIConfig.GRID_CELL_PADDING, y + UIConfig.GRID_CELL_PADDING),
                                size = Size(cellSize - UIConfig.GRID_CELL_PADDING * 2, cellSize - UIConfig.GRID_CELL_PADDING * 2),
                                isHighlighted = isHighlighted,
                                isHovered = isHoveredCol,
                                highlightColor = highlightColor
                            )
                        }

                        // Draw disc with animation offset
                        if (cell.isOccupied()) {
                            val discY = calculateDiscY(
                                row = row,
                                col = col,
                                y = y,
                                cellSize = cellSize,
                                animationState = animationState,
                                animationProgress = effectiveProgress
                            )

                            with(themeRenderer) {
                                drawDisc(
                                    disc = cell.discOrNull()!!,
                                    center = Offset(x + cellSize / 2, discY + cellSize / 2),
                                    radius = cellSize * GameConfig.DISC_RADIUS_FRACTION,
                                    textMeasurer = textMeasurer,
                                    isHighlighted = isHighlighted,
                                    highlightValue = highlightValue
                                )
                            }
                        }
                    }
                }

                // Draw new row during AddingNewRow animation (appears at 100% after discs finish moving)
                if (animationState is AnimationState.AddingNewRow) {
                    val bottomRow = GameConfig.GRID_SIZE - 1

                    // Only show new row when animation reaches 100% (after existing discs finish moving up)
                    if (effectiveProgress >= 1.0f) {
                        for (col in 0 until GameConfig.GRID_SIZE) {
                            val newRowCell = animationState.stateAfterNewRow.getCell(bottomRow, col)
                            if (newRowCell.isOccupied()) {
                                val x = col * cellSize
                                val y = bottomRow * cellSize // Final position (no animation)

                                with(themeRenderer) {
                                    drawCellBackground(
                                        topLeft = Offset(x + UIConfig.GRID_CELL_PADDING, y + UIConfig.GRID_CELL_PADDING),
                                        size = Size(cellSize - UIConfig.GRID_CELL_PADDING * 2, cellSize - UIConfig.GRID_CELL_PADDING * 2),
                                        isHighlighted = false,
                                        isHovered = false,
                                        highlightColor = null
                                    )

                                    drawDisc(
                                        disc = newRowCell.discOrNull()!!,
                                        center = Offset(x + cellSize / 2, y + cellSize / 2),
                                        radius = cellSize * GameConfig.DISC_RADIUS_FRACTION,
                                        textMeasurer = textMeasurer,
                                        isHighlighted = false
                                    )
                                }
                            }
                        }
                    }
                }

                // Draw grid lines
                with(themeRenderer) {
                    drawGridLines(size, cellSize)
                }

                // Draw game-over discs above grid
                val gameOverDiscs = if (gameState.status == GameStatus.GameOver) {
                    gameState.gameOverDiscs
                } else if (animationState is AnimationState.AddingNewRow &&
                           animationState.stateAfterNewRow.status == GameStatus.GameOver &&
                           effectiveProgress >= 1.0f) {
                    // Only show game-over discs after the new row animation completes
                    animationState.stateAfterNewRow.gameOverDiscs
                } else {
                    emptyMap()
                }

                for ((col, disc) in gameOverDiscs) {
                    // Skip if this disc is being animated by DroppingDisc
                    if (animationState is AnimationState.DroppingDisc &&
                        animationState.column == col) {
                        continue
                    }
                    val x = col * cellSize
                    val y = -cellSize

                    with(themeRenderer) {
                        drawCellBackground(
                            topLeft = Offset(x + GameConfig.GAME_OVER_RECT_PADDING, y + GameConfig.GAME_OVER_RECT_PADDING),
                            size = Size(cellSize - GameConfig.GAME_OVER_RECT_PADDING * 2, cellSize - GameConfig.GAME_OVER_RECT_PADDING * 2),
                            isHighlighted = true,
                            isHovered = false,
                            highlightColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = GameConfig.GAME_OVER_OVERLAY_ALPHA)
                        )

                        drawDisc(
                            disc = disc,
                            center = Offset(x + cellSize / 2, y + cellSize / 2),
                            radius = cellSize * GameConfig.DISC_RADIUS_FRACTION,
                            textMeasurer = textMeasurer,
                            isHighlighted = true,
                            isGameOverHighlight = true
                        )
                    }
                }

                // Draw dropping disc animation
                if (animationState is AnimationState.DroppingDisc) {
                    val x = animationState.column * cellSize
                    val startY = cellSize * GameConfig.PREVIEW_DISC_Y_OFFSET
                    val endY = if (animationState.targetRow == -1) {
                        -cellSize
                    } else {
                        animationState.targetRow * cellSize
                    }
                    val currentY = startY + (endY - startY) * effectiveProgress

                    with(themeRenderer) {
                        drawDroppingDisc(
                            disc = animationState.disc,
                            center = Offset(x + cellSize / 2, currentY + cellSize / 2),
                            radius = cellSize * GameConfig.DISC_RADIUS_FRACTION,
                            textMeasurer = textMeasurer,
                            effectiveProgress = effectiveProgress,
                            startY = startY,
                            endY = endY,
                            cellSize = cellSize
                        )
                    }
                }

                // Draw preview disc when idle
                if (animationState is AnimationState.Idle && gameState.status == GameStatus.Playing) {
                    val previewCol = hoveredColumn ?: (GameConfig.GRID_SIZE / 2)
                    val x = previewCol * cellSize
                    val y = cellSize * GameConfig.PREVIEW_DISC_Y_OFFSET
                    val alpha = if (hoveredColumn != null) GameConfig.PREVIEW_DISC_ALPHA_ACTIVE else GameConfig.PREVIEW_DISC_ALPHA_IDLE

                    with(themeRenderer) {
                        drawDisc(
                            disc = gameState.nextDisc,
                            center = Offset(x + cellSize / 2, y + cellSize / 2),
                            radius = cellSize * GameConfig.DISC_RADIUS_FRACTION,
                            textMeasurer = textMeasurer,
                            isHighlighted = false,
                            alpha = alpha
                        )
                    }
                }
            }

            // Floating point text
            floatingPoints.forEach { (position, points) ->
                val row = position.row.value
                val col = position.col.value
                val cellSizeFloat = canvasSize / GameConfig.GRID_SIZE
                val xPx = canvasOffset.x + (col * cellSizeFloat + cellSizeFloat / 2)
                val yPx = canvasOffset.y + (row * cellSizeFloat + cellSizeFloat / 2)

                val cell = gameState.getCell(row, col)
                val discValue = cell.discOrNull()?.numericValue ?: GameConfig.MIN_DISC_VALUE
                val discColor = themeRenderer.getDiscColor(discValue)

                with(density) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = xPx.toDp() - 15.dp,
                                y = yPx.toDp() - 50.dp
                            )
                    ) {
                        FloatingPointText(
                            points = points,
                            color = discColor,
                            onAnimationComplete = { }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates Y position for a disc considering animation state.
 */
private fun calculateDiscY(
    row: Int,
    col: Int,
    y: Float,
    cellSize: Float,
    animationState: AnimationState,
    animationProgress: Float
): Float {
    return when (animationState) {
        is AnimationState.ApplyingGravity -> {
            val targetPosition = GridPosition(Row(row), Col(col))
            val sourcePosition = animationState.movements.entries.find { it.value == targetPosition }?.key

            if (sourcePosition != null) {
                val fromRow = sourcePosition.row.value
                val startY = fromRow * cellSize
                val endY = row * cellSize
                startY + (endY - startY) * animationProgress
            } else {
                y
            }
        }
        is AnimationState.AddingNewRow -> {
            val targetRow = row - 1
            if (targetRow >= 0) {
                val startY = y
                val endY = targetRow * cellSize
                startY + (endY - startY) * animationProgress
            } else {
                val startY = y
                val endY = -cellSize
                startY + (endY - startY) * animationProgress
            }
        }
        else -> y
    }
}
