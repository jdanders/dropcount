package io.github.jdanders.dropseven.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Represents a single particle in an effect.
 */
data class Particle(
    val startPosition: Offset,
    val velocity: Offset,
    val color: Color,
    val size: Float,
    val lifetime: Long = 500L
)

/**
 * Creates particles for a break effect.
 */
fun createBreakParticles(
    centerPosition: Offset,
    color: Color,
    count: Int = 8
): List<Particle> {
    val particles = mutableListOf<Particle>()
    val angleStep = (2 * Math.PI) / count
    
    repeat(count) { i ->
        val angle = angleStep * i
        val speed = Random.nextFloat() * 50f + 50f
        val velocity = Offset(
            (cos(angle) * speed).toFloat(),
            (sin(angle) * speed).toFloat()
        )
        
        particles.add(
            Particle(
                startPosition = centerPosition,
                velocity = velocity,
                color = color,
                size = Random.nextFloat() * 4f + 4f,
                lifetime = Random.nextLong(300, 600)
            )
        )
    }
    
    return particles
}

/**
 * Calculates particle position at a given progress (0-1).
 */
fun calculateParticlePosition(particle: Particle, progress: Float): Offset {
    return Offset(
        particle.startPosition.x + particle.velocity.x * progress,
        particle.startPosition.y + particle.velocity.y * progress
    )
}

/**
 * Calculates particle alpha at a given progress (0-1).
 */
fun calculateParticleAlpha(progress: Float): Float {
    return 1f - progress
}

/**
 * State holder for particle effects.
 */
@Stable
class ParticleEffectState {
    var particles by mutableStateOf<List<Particle>>(emptyList())
        private set
    
    private var animationJob: kotlinx.coroutines.Job? = null
    
    fun triggerEffect(centerPosition: Offset, color: Color) {
        particles = createBreakParticles(centerPosition, color)
    }
    
    fun clear() {
        particles = emptyList()
        animationJob?.cancel()
    }
}

/**
 * Remembers a particle effect state.
 */
@Composable
fun rememberParticleEffectState(): ParticleEffectState {
    return remember { ParticleEffectState() }
}

