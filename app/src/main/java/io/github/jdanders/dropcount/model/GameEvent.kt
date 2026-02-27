package io.github.jdanders.dropcount.model

import io.github.jdanders.dropcount.model.Disc

/**
 * Events that occur during gameplay for analytics and tracking purposes.
 */
sealed class GameEvent {

    /**
     * Fired when a disc is dropped by the player.
     */
    data class DiscDropped(val column: Int, val disc: Disc) : GameEvent()

    /**
     * Fired when a chain reaction completes.
     */
    data class ChainCompleted(val level: Int, val score: Int) : GameEvent()

    /**
     * Fired when the player advances to a new level.
     */
    data class LevelUp(val newLevel: Int) : GameEvent()

    /**
     * Fired when the game ends.
     */
    data class GameOver(val finalScore: Int) : GameEvent()

    /**
     * Fired when the player undoes a move.
     */
    data object MoveUndone : GameEvent()

    /**
     * Fired when a new game is started.
     */
    data class GameStarted(val mode: GameMode) : GameEvent()
}

/**
 * Interface for listening to game events.
 * Implementations can track analytics, logging, or other side effects.
 */
interface GameEventListener {
    fun onEvent(event: GameEvent)
}