package io.github.jdanders.dropcount.viewmodel

import io.github.jdanders.dropcount.model.AnimationSpeed
import io.github.jdanders.dropcount.model.AnimationState
import io.github.jdanders.dropcount.model.GridPosition

/**
 * UI-related state that affects the visual presentation of the game.
 */
data class UIState(
    val hoveredColumn: Int? = null,
    val floatingPoints: Map<GridPosition, Int> = emptyMap(),
    val levelUpBonus: Int? = null,
    val boardClearBonus: Int? = null,
    val canUndo: Boolean = false
)

/**
 * Animation-related state and configuration.
 */
data class AnimationData(
    val state: AnimationState = AnimationState.Idle,
    val speed: AnimationSpeed = AnimationSpeed.MEDIUM
)