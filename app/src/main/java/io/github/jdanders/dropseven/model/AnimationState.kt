package io.github.jdanders.dropseven.model

/**
 * Represents different animation states during gameplay.
 */
sealed class AnimationState {
    object Idle : AnimationState()
    
    data class HighlightMatches(
        val positions: Set<Pair<Int, Int>>,
        val chainLevel: Int
    ) : AnimationState()
    
    data class RemovingDiscs(
        val positions: Set<Pair<Int, Int>>
    ) : AnimationState()
    
    data class DroppingDiscs(
        val movements: Map<Pair<Int, Int>, Pair<Int, Int>> // from -> to
    ) : AnimationState()
}

/**
 * Represents a single step in the chain reaction process.
 */
data class ChainStep(
    val stateBeforeRemoval: GameState,
    val matchPositions: Set<Pair<Int, Int>>, // Positions that will be removed
    val highlightPositions: Set<Pair<Int, Int>>, // All positions in contiguous regions (for highlighting)
    val stateAfterRemoval: GameState,
    val chainLevel: Int,
    val isFirstStepAfterNewRow: Boolean = false // True if this is the first match after a new row was added
)

/**
 * Result of dropping a disc with all animation steps.
 */
data class DropResult(
    val steps: List<ChainStep>,
    val finalState: GameState
)

