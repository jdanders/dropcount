package io.github.jdanders.dropcount.data

import io.github.jdanders.dropcount.model.GameState

/**
 * Repository interface for game state persistence operations.
 * Separates game state persistence from general preferences management.
 */
interface GameStateRepository {

    /**
     * Saves the current game state.
     * Only saves if the game is in Playing status.
     */
    suspend fun save(state: GameState)

    /**
     * Loads the saved game state, or null if none exists.
     */
    suspend fun load(): GameState?

    /**
     * Clears any saved game state.
     */
    suspend fun clear()
}