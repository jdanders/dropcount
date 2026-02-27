package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.model.Cell
import io.github.jdanders.dropcount.model.Disc
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.model.GameState
import io.github.jdanders.dropcount.model.GameStatus

object TestUtils {
    fun createEmptyGameState(mode: GameMode = GameMode.Normal): GameState {
        val emptyGrid = List(GameState.GRID_SIZE) { List(GameState.GRID_SIZE) { Cell.Empty } }
        return GameState(
            grid = emptyGrid,
            score = 0,
            totalDrops = 0,
            currentChain = 0,
            level = 1,
            mode = mode,
            status = GameStatus.Playing
        )
    }
}

/**
 * Extension for creating a GameState with specific discs for testing.
 */
fun GameState.withDiscs(vararg positions: Triple<Int, Int, Disc>): GameState {
    var state = this
    positions.forEach { (row, col, disc) ->
        state = state.setCell(row, col, Cell.Occupied(disc))
    }
    return state
}