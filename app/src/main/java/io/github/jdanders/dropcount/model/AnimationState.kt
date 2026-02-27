package io.github.jdanders.dropcount.model

/**
 * Represents different animation states during gameplay.
 */
sealed class AnimationState {
    /**
     * No animation in progress - ready for input.
     */
    object Idle : AnimationState()

    /**
     * Animating a disc drop from preview position to target position.
     * @param disc The disc being dropped
     * @param column The column where disc is being dropped
     * @param targetRow The target row (-1 for game over position above grid)
     */
    data class DroppingDisc(
        val disc: Disc,
        val column: Int,
        val targetRow: Int
    ) : AnimationState()

    /**
     * Highlighting matched discs before removal.
     * @param positions All positions that are part of contiguous regions (for highlighting)
     * @param colors Map of position to disc value for coloring highlights
     * @param chainLevel Current chain multiplier
     */
    data class HighlightingMatches(
        val positions: Set<GridPosition>,
        val colors: Map<GridPosition, Int>,
        val chainLevel: Int
    ) : AnimationState()

    /**
     * Animating discs falling due to gravity.
     * @param movements Map of from position -> to position
     */
    data class ApplyingGravity(
        val movements: Map<GridPosition, GridPosition>
    ) : AnimationState()

    /**
     * Animating new row addition (rows shifting up, new row appearing).
     * @param stateBeforeNewRow Game state before new row was added
     * @param stateAfterNewRow Game state after new row was added
     */
    data class AddingNewRow(
        val stateBeforeNewRow: GameState,
        val stateAfterNewRow: GameState
    ) : AnimationState()
}

/**
 * Represents a single step in the chain reaction process.
 */
data class ChainStep(
    val stateBeforeRemoval: GameState,
    val matchPositions: Set<GridPosition>, // Positions that will be removed
    val highlightPositions: Set<GridPosition>, // All positions in contiguous regions (for highlighting)
    val highlightColors: Map<GridPosition, Int>, // Map of position to disc value for coloring highlights
    val stateAfterRemoval: GameState,
    val gravityMovements: Map<GridPosition, GridPosition>, // Disc movements from gravity (from -> to)
    val chainLevel: Int,
    val isFirstStepAfterNewRow: Boolean = false, // True if this is the first match after a new row was added
    val discPointValues: Map<GridPosition, Int> = emptyMap() // Points awarded for each disappearing disc
)

/**
 * Result of dropping a disc with all animation steps.
 */
data class DropResult(
    val steps: List<ChainStep>, // Steps before new row (from dropped disc)
    val finalState: GameState,
    val stateBeforeNewRow: GameState? = null, // State before new row was added (for animation)
    val stepsAfterNewRow: List<ChainStep> = emptyList() // Steps after new row (from new row matches)
)
