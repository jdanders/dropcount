package io.github.jdanders.dropseven.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

/**
 * Animation state for a disc being dropped.
 */
data class DiscDropAnimation(
    val column: Int,
    val targetRow: Int,
    val progress: Float = 0f
)

/**
 * Animation state for discs breaking.
 */
data class DiscBreakAnimation(
    val row: Int,
    val column: Int,
    val progress: Float = 0f
)

/**
 * Creates an animated float value that bounces.
 */
@Composable
fun rememberBounceAnimation(
    initialValue: Float = 0f,
    targetValue: Float = 1f,
    trigger: Any?
): State<Float> {
    val animatedValue = remember { Animatable(initialValue) }
    
    LaunchedEffect(trigger) {
        if (trigger != null) {
            animatedValue.snapTo(initialValue)
            animatedValue.animateTo(
                targetValue,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    return animatedValue.asState()
}

/**
 * Creates an animated float value for pulse effect.
 */
@Composable
fun rememberPulseAnimation(enabled: Boolean): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    return if (enabled) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
}

/**
 * Creates an animated offset for disc drop animation.
 */
@Composable
fun rememberDiscDropOffset(
    startOffset: Offset,
    endOffset: Offset,
    trigger: Any?
): State<Offset> {
    val animatedX = remember { Animatable(startOffset.x) }
    val animatedY = remember { Animatable(startOffset.y) }
    
    LaunchedEffect(trigger) {
        if (trigger != null) {
            animatedX.snapTo(startOffset.x)
            animatedY.snapTo(startOffset.y)
            
            // Animate to end position
            animatedY.animateTo(
                endOffset.y,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    return derivedStateOf {
        Offset(animatedX.value, animatedY.value)
    }
}

/**
 * Creates a fade out animation.
 */
@Composable
fun rememberFadeOutAnimation(trigger: Any?): State<Float> {
    val animatedAlpha = remember { Animatable(1f) }
    
    LaunchedEffect(trigger) {
        if (trigger != null) {
            animatedAlpha.snapTo(1f)
            animatedAlpha.animateTo(
                0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = LinearEasing
                )
            )
        }
    }
    
    return animatedAlpha.asState()
}

/**
 * Creates a scale animation for breaking discs.
 */
@Composable
fun rememberBreakScaleAnimation(trigger: Any?): State<Float> {
    val animatedScale = remember { Animatable(1f) }
    
    LaunchedEffect(trigger) {
        if (trigger != null) {
            animatedScale.snapTo(1f)
            // Scale up then fade
            animatedScale.animateTo(
                1.5f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    return animatedScale.asState()
}

