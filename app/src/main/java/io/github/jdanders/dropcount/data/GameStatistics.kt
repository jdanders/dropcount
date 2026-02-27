package io.github.jdanders.dropcount.data

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.model.ChallengeDifficulty
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.util.Logger
import kotlinx.serialization.Serializable

/**
 * Container for all game mode statistics.
 */
@Serializable
data class AllGameStatistics(
    val normalMode: ModeGameStatistics = ModeGameStatistics(),
    val challengeEasy: ModeGameStatistics = ModeGameStatistics(),
    val challengeMedium: ModeGameStatistics = ModeGameStatistics(),
    val challengeHard: ModeGameStatistics = ModeGameStatistics(),
    val challengeExtreme: ModeGameStatistics = ModeGameStatistics(),
    val sequenceMode: ModeGameStatistics = ModeGameStatistics()
) {
    /**
     * Gets statistics for a specific game mode.
     */
    fun forMode(mode: GameMode): ModeGameStatistics {
        return when (mode) {
            is GameMode.Normal -> normalMode
            is GameMode.Challenge -> when (mode.difficulty) {
                ChallengeDifficulty.EASY -> challengeEasy
                ChallengeDifficulty.MEDIUM -> challengeMedium
                ChallengeDifficulty.HARD -> challengeHard
                ChallengeDifficulty.EXTREME -> challengeExtreme
            }
            is GameMode.Sequence -> sequenceMode
        }
    }
    
    /**
     * Adds a game result to the appropriate mode's statistics.
     */
    fun addGame(result: GameResult): AllGameStatistics {
        when (val mode = result.mode) {
            is GameMode.Normal -> {
                Logger.d("GameStatistics", "Adding game result to Normal mode: score=${result.score}")
                return copy(normalMode = normalMode.addGame(result))
            }
            is GameMode.Challenge -> {
                Logger.d("GameStatistics", "Adding game result to Challenge mode: difficulty=${mode.difficulty}, score=${result.score}")
                return when (mode.difficulty) {
                    ChallengeDifficulty.EASY -> copy(challengeEasy = challengeEasy.addGame(result))
                    ChallengeDifficulty.MEDIUM -> copy(challengeMedium = challengeMedium.addGame(result))
                    ChallengeDifficulty.HARD -> copy(challengeHard = challengeHard.addGame(result))
                    ChallengeDifficulty.EXTREME -> copy(challengeExtreme = challengeExtreme.addGame(result))
                }
            }
            is GameMode.Sequence -> {
                Logger.d("GameStatistics", "Adding game result to Sequence mode: seed=${mode.seed}, score=${result.score}")
                return copy(sequenceMode = sequenceMode.addGame(result))
            }
        }
    }
}

/**
 * Statistics tracked for a specific game mode's history.
 */
@Serializable
data class ModeGameStatistics(
    // All-time statistics (running totals)
    val totalScore: Long = 0,  // Use Long to avoid overflow
    val totalGamesPlayed: Int = 0,  // Track internally but don't display
    val highestScore: Int = 0,
    val highestLevel: Int = 0,
    val longestChain: Int = 0,
    val highestSingleMove: Int = 0,
    
    // Rolling window of recent games (limit to last GameConfig.MAX_RECENT_GAMES games)
    val recentGames: List<GameResult> = emptyList()
) {
    
    /**
     * Average score across all games played.
     */
    val averageScore: Double
        get() = if (totalGamesPlayed > 0) totalScore.toDouble() / totalGamesPlayed else 0.0
    
    /**
     * Average level reached across recent games.
     */
    val averageLevel: Double
        get() = if (recentGames.isNotEmpty()) {
            recentGames.map { it.level }.average()
        } else 0.0
    
    /**
     * Average chain length across recent games.
     */
    val averageChainLength: Double
        get() = if (recentGames.isNotEmpty()) {
            recentGames.map { it.longestChain }.average()
        } else 0.0
    
    /**
     * Average highest single move score across recent games.
     */
    val averageSingleMoveScore: Double
        get() = if (recentGames.isNotEmpty()) {
            recentGames.map { it.highestSingleMove }.average()
        } else 0.0
    
    /**
     * Add a new game result to statistics.
     */
    fun addGame(result: GameResult): ModeGameStatistics {
        // Update running totals
        val newTotalGames = totalGamesPlayed + 1
        val newTotalScore = totalScore + result.score
        val newHighestScore = maxOf(highestScore, result.score)
        val newHighestLevel = maxOf(highestLevel, result.level)
        val newLongestChain = maxOf(longestChain, result.longestChain)
        val newHighestSingleMove = maxOf(highestSingleMove, result.highestSingleMove)
        
        // Add to rolling window (keep only last GameConfig.MAX_RECENT_GAMES)
        val updatedRecentGames = (recentGames + result).takeLast(GameConfig.MAX_RECENT_GAMES)
        
        return copy(
            totalGamesPlayed = newTotalGames,
            totalScore = newTotalScore,
            highestScore = newHighestScore,
            highestLevel = newHighestLevel,
            longestChain = newLongestChain,
            highestSingleMove = newHighestSingleMove,
            recentGames = updatedRecentGames
        )
    }
}

/**
 * Result of a completed game.
 */
@Serializable
data class GameResult(
    val score: Int,
    val mode: GameMode,
    val level: Int,
    val longestChain: Int,
    val highestSingleMove: Int,
    val totalDrops: Int,
    val timestamp: Long = System.currentTimeMillis()
)


