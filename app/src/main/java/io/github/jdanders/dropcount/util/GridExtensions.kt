package io.github.jdanders.dropcount.util

import io.github.jdanders.dropcount.model.Cell
import io.github.jdanders.dropcount.model.Disc
import io.github.jdanders.dropcount.model.GameState
import io.github.jdanders.dropcount.model.GridPosition
import io.github.jdanders.dropcount.model.Row
import io.github.jdanders.dropcount.model.Col

/**
 * Extension functions for GameState grid operations.
 */

/**
 * Returns all positions that contain discs.
 */
fun GameState.getAllOccupiedPositions(): List<GridPosition> {
    val positions = mutableListOf<GridPosition>()
    for (row in 0 until GameState.GRID_SIZE) {
        for (col in 0 until GameState.GRID_SIZE) {
            if (grid[row][col].isOccupied()) {
                positions.add(GridPosition(Row(row), Col(col)))
            }
        }
    }
    return positions
}

/**
 * Gets the disc at the specified position, or null if the position is empty.
 */
fun GameState.getDiscAt(position: GridPosition): Disc? {
    return grid[position.row.value][position.col.value].discOrNull()
}

/**
 * Returns all positions in the grid that contain discs with the specified value.
 */
fun GameState.getPositionsWithValue(value: Int): List<GridPosition> {
    return getAllOccupiedPositions().filter { position ->
        getDiscAt(position)?.numericValue == value
    }
}