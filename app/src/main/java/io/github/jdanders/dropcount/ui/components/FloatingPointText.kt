package io.github.jdanders.dropcount.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.config.UIConfig
import kotlinx.coroutines.delay

/**
 * Displays a floating "+X" text that animates upward and fades out.
 */
@Composable
fun FloatingPointText(
    points: Int,
    onAnimationComplete: () -> Unit,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var isVisible by remember { mutableStateOf(true) }
    
    // Animate vertical offset (move up)
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else UIConfig.FLOATING_TEXT_OFFSET_Y,
        animationSpec = tween(
            durationMillis = UIConfig.FLOATING_TEXT_DURATION,
            easing = LinearOutSlowInEasing
        ),
        finishedListener = {
            onAnimationComplete()
        },
        label = "floatingOffset"
    )
    
    // Animate alpha (fade out)
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = UIConfig.FLOATING_TEXT_DURATION,
            easing = LinearEasing
        ),
        label = "floatingAlpha"
    )
    
    // Start animation after composition
    LaunchedEffect(Unit) {
        delay(UIConfig.FLOATING_TEXT_ANIMATION_DELAY) // Small delay before starting animation
        isVisible = false
    }
    
    val text = "+$points"
    val textStyle = TextStyle(
        fontSize = UIConfig.FLOATING_TEXT_FONT_SIZE.sp,
        fontWeight = FontWeight.Bold
    )
    val textLayoutResult = textMeasurer.measure(text, textStyle)
    
    Box(
        modifier = modifier.offset { IntOffset(0, offsetY.toInt()) }
    ) {
        Canvas(
            modifier = Modifier.size(
                width = with(density) { textLayoutResult.size.width.toDp() + 8.dp },
                height = with(density) { textLayoutResult.size.height.toDp() + 8.dp }
            )
        ) {
            val textColor = Color.White.copy(alpha = alpha)
            val outlineColor = color.copy(alpha = alpha)
            val outlineWidth = UIConfig.FLOATING_TEXT_OUTLINE_WIDTH
            
            val centerX = size.width / 2
            val centerY = size.height / 2
            val textOffset = Offset(
                centerX - textLayoutResult.size.width / 2,
                centerY - textLayoutResult.size.height / 2
            )
            
            // Draw colored outline by drawing text at multiple offsets
            val outlineOffsets = listOf(
                Offset(-outlineWidth, -outlineWidth),
                Offset(-outlineWidth, 0f),
                Offset(-outlineWidth, outlineWidth),
                Offset(0f, -outlineWidth),
                Offset(0f, outlineWidth),
                Offset(outlineWidth, -outlineWidth),
                Offset(outlineWidth, 0f),
                Offset(outlineWidth, outlineWidth)
            )
            
            // Draw outline (colored)
            outlineOffsets.forEach { offset ->
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textOffset + offset,
                    color = outlineColor
                )
            }
            
            // Draw main text on top (white)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textOffset,
                color = textColor
            )
        }
    }
}

