package io.github.jdanders.dropseven.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropseven.model.Cell
import io.github.jdanders.dropseven.model.Disc
import io.github.jdanders.dropseven.model.GameState
import io.github.jdanders.dropseven.model.GameStatus
import io.github.jdanders.dropseven.ui.theme.*

@Composable
fun GameGrid(
    gameState: GameState,
    onColumnTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightedPositions: Set<Pair<Int, Int>> = emptySet()
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / GameState.GRID_SIZE
                    val column = (offset.x / cellSize).toInt().coerceIn(0, GameState.GRID_SIZE - 1)
                    onColumnTap(column)
                }
            }
    ) {
        val cellSize = size.width / GameState.GRID_SIZE
        val isGameOver = gameState.status == GameStatus.GameOver && gameState.gameOverDiscs.isNotEmpty()
        
        // Draw grid background
        drawRect(
            color = GridLineColor,
            topLeft = Offset.Zero,
            size = size
        )
        
        // Draw normal 7x7 grid cells
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                val cell = gameState.getCell(row, col)
                val x = col * cellSize
                val y = row * cellSize
                val isHighlighted = highlightedPositions.contains(row to col)
                
                // Draw cell background
                drawRect(
                    color = if (isHighlighted) Color.Yellow.copy(alpha = 0.3f) else BackgroundDark,
                    topLeft = Offset(x + 2f, y + 2f),
                    size = Size(cellSize - 4f, cellSize - 4f)
                )
                
                // Draw disc if present
                if (cell is Cell.Occupied) {
                    drawDisc(
                        disc = cell.disc,
                        center = Offset(x + cellSize / 2, y + cellSize / 2),
                        radius = cellSize * 0.4f,
                        textMeasurer = textMeasurer,
                        isHighlighted = isHighlighted
                    )
                }
            }
        }
        
        // Draw grid lines (normal 7x7 grid)
        for (i in 0..GameState.GRID_SIZE) {
            val pos = i * cellSize
            // Vertical lines
            drawLine(
                color = GridLineColor,
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = 2f
            )
            // Horizontal lines
            drawLine(
                color = GridLineColor,
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = 2f
            )
        }
        
        // Draw game-over discs ABOVE the grid if game is over
        if (isGameOver) {
            for ((col, disc) in gameState.gameOverDiscs) {
                val x = col * cellSize
                val y = -cellSize // One full cell above the grid
                
                // Draw a red background to show this is the illegal position
                drawRect(
                    color = Color.Red.copy(alpha = 0.7f),
                    topLeft = Offset(x + 2f, y + 2f),
                    size = Size(cellSize - 4f, cellSize - 4f)
                )
                
                // Draw the disc that was pushed out
                drawDisc(
                    disc = disc,
                    center = Offset(x + cellSize / 2, y + cellSize / 2),
                    radius = cellSize * 0.4f,
                    textMeasurer = textMeasurer,
                    isHighlighted = true
                )
            }
        }
    }
}

private fun DrawScope.drawDisc(
    disc: Disc,
    center: Offset,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    isHighlighted: Boolean = false
) {
    when (disc) {
        is Disc.Numbered -> {
            // Draw colored circle
            val color = getDiscColor(disc.value)
            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
            
            // Draw number
            val textLayoutResult = textMeasurer.measure(
                text = disc.value.toString(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (radius * 1.2f).sp
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    center.x - textLayoutResult.size.width / 2,
                    center.y - textLayoutResult.size.height / 2
                )
            )
            
            // Draw highlight border if highlighted
            if (isHighlighted) {
                drawCircle(
                    color = Color.Yellow,
                    radius = radius * 1.15f,
                    center = center,
                    style = Stroke(width = 6f)
                )
            }
        }
        is Disc.Solid -> {
            val color = if (disc.cracks == 0) SolidDiscColor else CrackedDiscColor
            
            // Draw solid/cracked circle
            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
            
            // Draw crack lines if cracked
            if (disc.cracks > 0) {
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(center.x - radius * 0.6f, center.y - radius * 0.3f),
                    end = Offset(center.x + radius * 0.4f, center.y + radius * 0.5f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(center.x - radius * 0.3f, center.y - radius * 0.5f),
                    end = Offset(center.x + radius * 0.5f, center.y + radius * 0.3f),
                    strokeWidth = 3f
                )
            }
            
            // Draw circle outline
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2f)
            )
            
            // Draw highlight border if highlighted (solid discs can be adjacent)
            if (isHighlighted) {
                drawCircle(
                    color = Color.Yellow,
                    radius = radius * 1.15f,
                    center = center,
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}

private fun getDiscColor(value: Int): Color {
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

