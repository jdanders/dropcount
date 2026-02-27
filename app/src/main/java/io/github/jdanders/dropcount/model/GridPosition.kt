package io.github.jdanders.dropcount.model

import io.github.jdanders.dropcount.config.GameConfig

/**
 * Type-safe Row index (0 to GRID_SIZE-1).
 */
@JvmInline
value class Row(val value: Int) {
    init {
        require(value in 0 until GameConfig.GRID_SIZE) { "Row must be between 0 and ${GameConfig.GRID_SIZE - 1}" }
    }
}

/**
 * Type-safe Column index (0 to GRID_SIZE-1).
 */
@JvmInline
value class Col(val value: Int) {
    init {
        require(value in 0 until GameConfig.GRID_SIZE) { "Column must be between 0 and ${GameConfig.GRID_SIZE - 1}" }
    }
}

/**
 * Type-safe grid position combining Row and Col.
 */
data class GridPosition(val row: Row, val col: Col) {
    
    val rowInt get() = row.value
    val colInt get() = col.value

    companion object {
        fun from(row: Int, col: Int) = GridPosition(Row(row), Col(col))
    }
}