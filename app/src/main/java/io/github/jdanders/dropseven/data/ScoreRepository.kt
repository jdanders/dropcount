package io.github.jdanders.dropseven.data

import androidx.datastore.preferences.core.Preferences
import io.github.jdanders.dropseven.model.ChallengeDifficulty
import io.github.jdanders.dropseven.model.GameMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing high scores based on game mode.
 */
class ScoreRepository(private val preferencesManager: PreferencesManager) {
    
    /**
     * Gets the high score for a specific game mode.
     */
    fun getHighScore(mode: GameMode): Flow<Int> {
        return when (mode) {
            is GameMode.Normal -> preferencesManager.highScoreNormal
            is GameMode.Challenge -> {
                when (mode.difficulty) {
                    ChallengeDifficulty.EASY -> preferencesManager.highScoreChallengeEasy
                    ChallengeDifficulty.MEDIUM -> preferencesManager.highScoreChallengeMedium
                    ChallengeDifficulty.HARD -> preferencesManager.highScoreChallengeHard
                    ChallengeDifficulty.EXTREME -> preferencesManager.highScoreChallengeExtreme
                }
            }
            is GameMode.Sequence -> preferencesManager.highScoreSequence
        }
    }
    
    /**
     * Saves a high score for a specific game mode.
     */
    suspend fun saveHighScore(mode: GameMode, score: Int) {
        val key = getPreferenceKey(mode)
        preferencesManager.saveHighScore(key, score)
    }
    
    /**
     * Gets the preference key for a specific game mode.
     */
    private fun getPreferenceKey(mode: GameMode): Preferences.Key<Int> {
        return when (mode) {
            is GameMode.Normal -> PreferencesManager.HIGH_SCORE_NORMAL
            is GameMode.Challenge -> {
                when (mode.difficulty) {
                    ChallengeDifficulty.EASY -> PreferencesManager.HIGH_SCORE_CHALLENGE_EASY
                    ChallengeDifficulty.MEDIUM -> PreferencesManager.HIGH_SCORE_CHALLENGE_MEDIUM
                    ChallengeDifficulty.HARD -> PreferencesManager.HIGH_SCORE_CHALLENGE_HARD
                    ChallengeDifficulty.EXTREME -> PreferencesManager.HIGH_SCORE_CHALLENGE_EXTREME
                }
            }
            is GameMode.Sequence -> PreferencesManager.HIGH_SCORE_SEQUENCE
        }
    }
}

