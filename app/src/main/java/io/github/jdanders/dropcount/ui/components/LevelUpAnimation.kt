package io.github.jdanders.dropcount.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.config.UIConfig
import io.github.jdanders.dropcount.R
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

/**
 * Displays a large "+X" level bonus animation that fades out.
 * Shows celebration for reaching a new level.
 */
@Composable
fun LevelUpAnimation(
    bonusPoints: Int,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var isVisible by remember { mutableStateOf(true) }

    // Animate alpha (fade out)
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = UIConfig.LEVEL_UP_ANIMATION_DURATION,
            easing = LinearEasing
        ),
        finishedListener = {
            onAnimationComplete()
        },
        label = "levelUpAlpha"
    )

    // Start animation after composition
    LaunchedEffect(Unit) {
        delay(UIConfig.LEVEL_UP_ANIMATION_DELAY) // Small delay before starting fade out
        isVisible = false
    }

    val text = stringResource(R.string.level_bonus_format, bonusPoints)
    val textStyle = TextStyle(
        fontSize = UIConfig.LEVEL_UP_FONT_SIZE.sp,
        fontWeight = FontWeight.Light
    )
    val textLayoutResult = textMeasurer.measure(text, textStyle)

    Box(
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier.size(
                width = with(density) { textLayoutResult.size.width.toDp() + 16.dp },
                height = with(density) { textLayoutResult.size.height.toDp() + 16.dp }
            )
        ) {
            val textColor = Color(0xFFFFA500).copy(alpha = alpha) // Orange color

            val centerX = this.size.width / 2
            val centerY = this.size.height / 2

            val textOffset = Offset(
                centerX - textLayoutResult.size.width / 2,
                centerY - textLayoutResult.size.height / 2
            )

            // Draw main text (orange)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textOffset,
                color = textColor
            )
        }
    }
}