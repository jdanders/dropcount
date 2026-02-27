package io.github.jdanders.dropcount.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jdanders.dropcount.model.GameState
import io.github.jdanders.dropcount.model.GameStatus
import io.github.jdanders.dropcount.util.Logger
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * DataStore-based implementation of GameStateRepository.
 * Uses the shared DataStore instance from [dropCountDataStore] to avoid
 * creating multiple DataStore instances for the same file (which causes crashes).
 */
class DataStoreGameStateRepository(
    private val context: Context
) : GameStateRepository {

    private val json = Json {
        allowStructuredMapKeys = true
        ignoreUnknownKeys = true
    }
    private val SAVED_GAME_STATE = stringPreferencesKey("saved_game_state")

    override suspend fun save(state: GameState) {
        try {
            // Only save if game is actively being played or paused
            if (state.status != GameStatus.Playing && state.status != GameStatus.Paused) {
                Logger.d("DataStoreGameStateRepository", "Not saving game state - game is not playing or paused (status=${state.status})")
                return
            }

            context.dropCountDataStore.edit { preferences ->
                val jsonString = json.encodeToString(GameState.serializer(), state)
                preferences[SAVED_GAME_STATE] = jsonString
                Logger.d("DataStoreGameStateRepository", "Game state saved successfully: score=${state.score}, level=${state.level}, mode=${state.mode}, totalDrops=${state.totalDrops}")
            }
        } catch (e: Exception) {
            Logger.e("DataStoreGameStateRepository", "Error saving game state", e)
        }
    }

    override suspend fun load(): GameState? {
        return try {
            val preferences = context.dropCountDataStore.data.first()
            val jsonString = preferences[SAVED_GAME_STATE]

            if (jsonString != null) {
                try {
                    val result = json.decodeFromString(GameState.serializer(), jsonString)
                    Logger.d("DataStoreGameStateRepository", "Loaded game state: score=${result.score}, level=${result.level}")
                    result
                } catch (e: Exception) {
                    Logger.e("DataStoreGameStateRepository", "Error decoding game state", e)
                    null
                }
            } else {
                Logger.d("DataStoreGameStateRepository", "No saved game state found")
                null
            }
        } catch (e: Exception) {
            Logger.e("DataStoreGameStateRepository", "Error loading game state", e)
            null
        }
    }

    override suspend fun clear() {
        try {
            context.dropCountDataStore.edit { preferences ->
                preferences.remove(SAVED_GAME_STATE)
                Logger.d("DataStoreGameStateRepository", "Cleared saved game state")
            }
        } catch (e: Exception) {
            Logger.e("DataStoreGameStateRepository", "Error clearing game state", e)
        }
    }
}