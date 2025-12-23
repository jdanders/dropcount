package io.github.jdanders.dropseven.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "drop_seven_prefs")

/**
 * Manages user preferences and high scores using DataStore.
 */
class PreferencesManager(private val context: Context) {
    
    companion object {
        // High score keys for each mode
        val HIGH_SCORE_NORMAL = intPreferencesKey("high_score_normal")
        val HIGH_SCORE_CHALLENGE_EASY = intPreferencesKey("high_score_challenge_easy")
        val HIGH_SCORE_CHALLENGE_MEDIUM = intPreferencesKey("high_score_challenge_medium")
        val HIGH_SCORE_CHALLENGE_HARD = intPreferencesKey("high_score_challenge_hard")
        val HIGH_SCORE_CHALLENGE_EXTREME = intPreferencesKey("high_score_challenge_extreme")
        val HIGH_SCORE_SEQUENCE = intPreferencesKey("high_score_sequence")
        
        // Settings keys
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }
    
    /**
     * Gets the high score for Normal mode.
     */
    val highScoreNormal: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_NORMAL] ?: 0 }
    
    /**
     * Gets the high score for Challenge Easy mode.
     */
    val highScoreChallengeEasy: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_CHALLENGE_EASY] ?: 0 }
    
    /**
     * Gets the high score for Challenge Medium mode.
     */
    val highScoreChallengeMedium: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_CHALLENGE_MEDIUM] ?: 0 }
    
    /**
     * Gets the high score for Challenge Hard mode.
     */
    val highScoreChallengeHard: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_CHALLENGE_HARD] ?: 0 }
    
    /**
     * Gets the high score for Challenge Extreme mode.
     */
    val highScoreChallengeExtreme: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_CHALLENGE_EXTREME] ?: 0 }
    
    /**
     * Gets the high score for Sequence mode.
     */
    val highScoreSequence: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[HIGH_SCORE_SEQUENCE] ?: 0 }
    
    /**
     * Gets whether sound is enabled.
     */
    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SOUND_ENABLED] ?: true }
    
    /**
     * Gets whether haptics are enabled.
     */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[HAPTICS_ENABLED] ?: true }
    
    /**
     * Saves a high score for a specific mode key.
     */
    suspend fun saveHighScore(key: Preferences.Key<Int>, score: Int) {
        context.dataStore.edit { preferences ->
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
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }
    
    /**
     * Sets whether haptics are enabled.
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }
}

