package io.github.jdanders.dropcount.config

import kotlin.math.pow

/**
 * Scoring formula calculations for chain reactions.
 * Uses the exact cubic formula: (7/6)n^3 + 12n^2 - (73/6)n + 6
 */
object ScoringFormula {
    private const val CUBIC_COEFFICIENT = 7.0 / 6.0
    private const val QUADRATIC_COEFFICIENT = 12.0
    private const val LINEAR_COEFFICIENT = -73.0 / 6.0
    private const val CONSTANT_TERM = 6.0

    /**
     * Calculates the score gain for a chain reaction of the given level.
     * Higher chain levels result in exponentially increasing scores.
     */
    fun calculateScoreGain(chainLevel: Int): Int {
        val n = chainLevel.toDouble()
        val score = CUBIC_COEFFICIENT * n.pow(3) +
                   QUADRATIC_COEFFICIENT * n.pow(2) +
                   LINEAR_COEFFICIENT * n +
                   CONSTANT_TERM
        return score.toInt()
    }
}