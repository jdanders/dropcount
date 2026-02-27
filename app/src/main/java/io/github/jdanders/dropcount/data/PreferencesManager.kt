package io.github.jdanders.dropcount.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.util.Logger
import io.github.jdanders.dropcount.model.GameState
import io.github.jdanders.dropcount.model.AnimationSpeed

internal val Context.dropCountDataStore: DataStore<Preferences> by preferencesDataStore(name = "dropcount_prefs")

/**
 * Manages user preferences and high scores using DataStore.
 */
class PreferencesManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    /**
     * Helper function to create a Flow for a high score preference key.
     */
    private fun getHighScoreFlow(key: Preferences.Key<Int>): Flow<Int> {
        return context.dropCountDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    /**
     * Gets the high score for Normal mode.
     */
    val highScoreNormal: Flow<Int> = getHighScoreFlow(HIGH_SCORE_NORMAL)

    /**
     * Gets the high score for Challenge Easy mode.
     */
    val highScoreChallengeEasy: Flow<Int> = getHighScoreFlow(HIGH_SCORE_CHALLENGE_EASY)

    /**
     * Gets the high score for Challenge Medium mode.
     */
    val highScoreChallengeMedium: Flow<Int> = getHighScoreFlow(HIGH_SCORE_CHALLENGE_MEDIUM)

    /**
     * Gets the high score for Challenge Hard mode.
     */
    val highScoreChallengeHard: Flow<Int> = getHighScoreFlow(HIGH_SCORE_CHALLENGE_HARD)

    /**
     * Gets the high score for Challenge Extreme mode.
     */
    val highScoreChallengeExtreme: Flow<Int> = getHighScoreFlow(HIGH_SCORE_CHALLENGE_EXTREME)

    /**
     * Gets the high score for Sequence mode.
     */
    val highScoreSequence: Flow<Int> = getHighScoreFlow(HIGH_SCORE_SEQUENCE)

    /**
     * Gets whether the user has seen the tutorial.
     */
    val hasSeenTutorial: Flow<Boolean> = context.dropCountDataStore.data
        .map { preferences -> preferences[HAS_SEEN_TUTORIAL] ?: false }

    /**
     * Sets whether the user has seen the tutorial.
     */
    suspend fun setHasSeenTutorial(hasSeen: Boolean) {
        context.dropCountDataStore.edit { preferences ->
            preferences[HAS_SEEN_TUTORIAL] = hasSeen
        }
    }

    /**
     * Gets whether sound is enabled.
     */
    val soundEnabled: Flow<Boolean> = context.dropCountDataStore.data
        .map { preferences -> preferences[SOUND_ENABLED] ?: true }

    /**
     * Gets whether haptics are enabled.
     */
    val hapticsEnabled: Flow<Boolean> = context.dropCountDataStore.data
        .map { preferences -> preferences[HAPTICS_ENABLED] ?: true }

    /**
     * Gets the current animation speed setting.
     */
    val animationSpeed: Flow<AnimationSpeed> = context.dropCountDataStore.data
        .map { preferences ->
            val speedName = preferences[ANIMATION_SPEED]
            if (speedName != null) {
                try {
                    AnimationSpeed.valueOf(speedName)
                } catch (e: IllegalArgumentException) {
                    Logger.e("PreferencesManager", "Invalid animation speed: $speedName", e)
                    AnimationSpeed.MEDIUM
                }
            } else {
                AnimationSpeed.MEDIUM
            }
        }

    /**
     * Gets the current visual theme setting.
     */
    val visualTheme: Flow<VisualTheme> = context.dropCountDataStore.data
        .map { preferences ->
            val themeName = preferences[VISUAL_THEME]
            if (themeName != null) {
                try {
                    VisualTheme.valueOf(themeName)
                } catch (e: IllegalArgumentException) {
                    Logger.e("PreferencesManager", "Invalid visual theme: $themeName", e)
                    VisualTheme.CLASSIC
                }
            } else {
                VisualTheme.CLASSIC
            }
        }

    /**
     * Saves a high score for a specific mode key.
     */
    suspend fun saveHighScore(key: Preferences.Key<Int>, score: Int) {
        context.dropCountDataStore.edit { preferences ->
            val currentHighScore = preferences[key] ?: 0
            if (score > currentHighScore) {
                preferences[key] = score
            }
        }
    }

    /**
     * Sets whether sound is enabled.
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dropCountDataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    /**
     * Sets whether haptics are enabled.
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dropCountDataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }

    /**
     * Sets the animation speed.
     */
    suspend fun setAnimationSpeed(speed: AnimationSpeed) {
        context.dropCountDataStore.edit { preferences ->
            preferences[ANIMATION_SPEED] = speed.name
        }
    }

    /**
     * Sets the visual theme.
     */
    suspend fun setVisualTheme(theme: VisualTheme) {
        context.dropCountDataStore.edit { preferences ->
            preferences[VISUAL_THEME] = theme.name
        }
    }

    /**
     * Gets all game statistics (for all modes).
     */
    val gameStatistics: Flow<AllGameStatistics> = context.dropCountDataStore.data
        .map { preferences ->
            val jsonString = preferences[GAME_STATISTICS]
            Logger.d( "PreferencesManager", "Loading statistics, jsonString present: ${jsonString != null}, length: ${jsonString?.length ?: 0}")
            if (jsonString != null) {
                try {
                    val stats = json.decodeFromString(AllGameStatistics.serializer(), jsonString)
                    Logger.d( "PreferencesManager", "Decoded statistics: normalMode.totalGamesPlayed=${stats.normalMode.totalGamesPlayed}, highestScore=${stats.normalMode.highestScore}")
                    stats
                } catch (e: Exception) {
                    Logger.e( "PreferencesManager", "Error decoding statistics", e)
                    AllGameStatistics()
                }
            } else {
                Logger.d( "PreferencesManager", "No statistics found in DataStore, returning empty")
                AllGameStatistics()
            }
        }

    /**
     * Adds a game result to the appropriate mode's statistics.
     * Reads current stats from DataStore, adds the game result, and saves using saveGameStatistics.
     */
    suspend fun addGameResult(result: GameResult) {
        try {
            Logger.d( "PreferencesManager", "Adding game result: score=${result.score}, mode=${result.mode}")

            // Atomic read-modify-write in single edit block
            context.dropCountDataStore.edit { preferences ->
                // Read current statistics
                val currentStatsJson = preferences[GAME_STATISTICS]
                val currentStats: AllGameStatistics = if (currentStatsJson != null) {
                    try {
                        json.decodeFromString(AllGameStatistics.serializer(), currentStatsJson)
                    } catch (e: Exception) {
                        Logger.e( "PreferencesManager", "Error decoding current stats, starting fresh", e)
                        AllGameStatistics()
                    }
                } else {
                    AllGameStatistics()
                }

                // Add game result
                val updatedStats: AllGameStatistics = currentStats.addGame(result)

                // Save back to DataStore (still in same edit block = atomic)
                val updatedJson: String = json.encodeToString(AllGameStatistics.serializer(), updatedStats)
                preferences[GAME_STATISTICS] = updatedJson

                Logger.d( "PreferencesManager", "Statistics updated atomically in DataStore")
            }
        } catch (e: Exception) {
            Logger.e( "PreferencesManager", "Error in addGameResult", e)
            // Re-throw to let caller know the operation failed
            throw e
        }
    }

    companion object {
        // High score keys for each mode
        val HIGH_SCORE_NORMAL = intPreferencesKey("high_score_normal")
        val HIGH_SCORE_CHALLENGE_EASY = intPreferencesKey("high_score_challenge_easy")
        val HIGH_SCORE_CHALLENGE_MEDIUM = intPreferencesKey("high_score_challenge_medium")
        val HIGH_SCORE_CHALLENGE_HARD = intPreferencesKey("high_score_challenge_hard")
        val HIGH_SCORE_CHALLENGE_EXTREME = intPreferencesKey("high_score_challenge_extreme")
        val HIGH_SCORE_SEQUENCE = intPreferencesKey("high_score_sequence")

        // Statistics key
        val GAME_STATISTICS = stringPreferencesKey("game_statistics")

        // Settings keys
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val VISUAL_THEME = stringPreferencesKey("visual_theme")
        val HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
    }
}
