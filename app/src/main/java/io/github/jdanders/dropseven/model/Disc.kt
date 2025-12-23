package io.github.jdanders.dropseven.model

/**
 * Represents a disc in the game.
 */
sealed class Disc {
    /**
     * A numbered disc (1-7) that breaks when the count in its row or column matches its value.
     */
    data class Numbered(val value: Int) : Disc() {
        init {
            require(value in 1..7) { "Disc value must be between 1 and 7" }
        }
    }
    
    /**
     * A solid disc that needs to be cracked twice before revealing a number.
     * @param cracks Number of times adjacent discs have broken (0, 1, or 2)
     * @param hiddenValue The number that will be revealed when fully cracked
     */
    data class Solid(val cracks: Int = 0, val hiddenValue: Int = 0) : Disc() {
        init {
            require(cracks in 0..2) { "Cracks must be 0, 1, or 2" }
            require(hiddenValue in 1..7) { "Hidden value must be between 1 and 7" }
        }
        
        /**
         * Returns a new Solid disc with incremented crack count.
         * If fully cracked (2 cracks), returns a Numbered disc.
         */
        fun addCrack(): Disc {
            return when (cracks) {
                0 -> Solid(1, hiddenValue)
                1 -> Numbered(hiddenValue)
                else -> this // Already fully cracked
            }
        }
        
        val isFullyCracked: Boolean
            get() = cracks >= 2
    }
    
    /**
     * Returns true if this is a numbered disc (including revealed solid discs).
     */
    fun isNumbered(): Boolean = this is Numbered
    
    /**
     * Gets the visible number if this is a numbered disc, or null otherwise.
     */
    fun getNumber(): Int? = (this as? Numbered)?.value
}

