package io.github.jdanders.dropcount.model

import io.github.jdanders.dropcount.config.GameConfig
import kotlinx.serialization.Serializable

/**
 * Represents the complete state of the game.
 */
@Serializable
data class GameState(
    val grid: List<List<Cell>> = List(GameConfig.GRID_SIZE) { List(GameConfig.GRID_SIZE) { Cell.Empty } },
    val nextDisc: Disc = Disc.Numbered(GameConfig.MIN_DISC_VALUE),
    val score: Int = 0,
    val totalDrops: Int = 0,
    val dropsUntilNewRow: Int = GameConfig.NORMAL_MODE_INITIAL_DROPS_PER_ROW,
    val baseDropsPerRow: Int = GameConfig.NORMAL_MODE_INITIAL_DROPS_PER_ROW,
    val mode: GameMode = GameMode.Normal,
    val status: GameStatus = GameStatus.Playing,
    val currentChain: Int = 0,
    val level: Int = 1,
    val gameOverDiscs: Map<Int, Disc> = emptyMap(), // Column -> Disc that was pushed out
    val longestChain: Int = 0, // Longest chain achieved in this game
    val highestSingleScore: Int = 0, // Most points earned in a single action
    val randomSeed: Long = 0L, // Seed for random generator (for undo support, set by GameViewModel)
    val recentDiscs: List<Disc> = emptyList() // History of generated discs for run limiting consistency
) {
    /**
     * Gets the cell at the specified position.
     */
    fun getCell(row: Int, col: Int): Cell {
        require(row in SIZE_RANGE && col in SIZE_RANGE) {
            "Position ($row, $col) is out of bounds"
        }
        return grid[row][col]
    }

    /**
     * Gets the cell at the specified grid position.
     */
    fun getCell(position: GridPosition): Cell {
        return grid[position.row.value][position.col.value]
    }

    /**
     * Returns a new GameState with the cell at the specified position updated.
     */
    fun setCell(row: Int, col: Int, cell: Cell): GameState {
        require(row in SIZE_RANGE && col in SIZE_RANGE) {
            "Position ($row, $col) is out of bounds"
        }
        val newGrid = grid.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, cellValue ->
                    if (c == col) cell else cellValue
                }
            } else {
                rowList
            }
        }
        return copy(grid = newGrid)
    }

    /**
     * Returns a new GameState with the cell at the specified grid position updated.
     */
    fun setCell(position: GridPosition, cell: Cell): GameState {
        return setCell(position.row.value, position.col.value, cell)
    }

    /**
     * Checks if a column is full (top row is occupied).
     */
    fun isColumnFull(col: Int): Boolean {
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }
        return grid[0][col].isOccupied()
    }

    /**
     * Checks if the entire grid is full (all columns are full).
     */
    fun isGridFull(): Boolean {
        return (0 until GRID_SIZE).all { isColumnFull(it) }
    }

    /**
     * Gets the first empty row in a column (from bottom to top).
     * Returns null if the column is full.
     */
    fun getFirstEmptyRow(col: Int): Int? {
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }
        for (row in (GRID_SIZE - 1) downTo 0) {
            if (grid[row][col].isEmpty()) {
                return row
            }
        }
        return null
    }

    /**
     * Counts the number of discs in a specific row.
     */
    fun countDiscsInRow(row: Int): Int {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        return grid[row].count { it.isOccupied() }
    }

    /**
     * Counts the number of discs in a specific column.
     */
    fun countDiscsInColumn(col: Int): Int {
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }
        return grid.count { it[col].isOccupied() }
    }

    /**
     * Counts contiguous (adjacent) discs in a row starting from a position.
     * Counts left and right until hitting a gap or edge.
     */
    fun countContiguousDiscsInRow(row: Int, col: Int): Int {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }

        var count = 0

        // Count left (including current position)
        var c = col
        while (c >= 0 && grid[row][c].isOccupied()) {
            count++
            c--
        }

        // Count right (excluding current position to avoid double-counting)
        c = col + 1
        while (c < GRID_SIZE && grid[row][c].isOccupied()) {
            count++
            c++
        }

        return count
    }

    /**
     * Counts contiguous (adjacent) discs in a column starting from a position.
     * Counts up and down until hitting a gap or edge.
     */
    fun countContiguousDiscsInColumn(row: Int, col: Int): Int {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }

        var count = 0

        // Count up (including current position)
        var r = row
        while (r >= 0 && grid[r][col].isOccupied()) {
            count++
            r--
        }

        // Count down (excluding current position to avoid double-counting)
        r = row + 1
        while (r < GRID_SIZE && grid[r][col].isOccupied()) {
            count++
            r++
        }

        return count
    }

    /**
     * Gets all positions in the contiguous region for a given position in a row.
     */
    fun getContiguousRegionInRow(row: Int, col: Int): Set<GridPosition> {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }

        if (!grid[row][col].isOccupied()) return emptySet()

        val positions = mutableSetOf<GridPosition>()

        // Find left boundary
        var c = col
        while (c >= 0 && grid[row][c].isOccupied()) {
            positions.add(GridPosition(Row(row), Col(c)))
            c--
        }

        // Find right boundary
        c = col + 1
        while (c < GRID_SIZE && grid[row][c].isOccupied()) {
            positions.add(GridPosition(Row(row), Col(c)))
            c++
        }

        return positions
    }

    /**
     * Gets all positions in the contiguous region for a given position in a column.
     */
    fun getContiguousRegionInColumn(row: Int, col: Int): Set<GridPosition> {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }

        if (!grid[row][col].isOccupied()) return emptySet()

        val positions = mutableSetOf<GridPosition>()

        // Find top boundary
        var r = row
        while (r >= 0 && grid[r][col].isOccupied()) {
            positions.add(GridPosition(Row(r), Col(col)))
            r--
        }

        // Find bottom boundary
        r = row + 1
        while (r < GRID_SIZE && grid[r][col].isOccupied()) {
            positions.add(GridPosition(Row(r), Col(col)))
            r++
        }

        return positions
    }

    /**
     * Returns all positions in a row.
     */
    fun getRowPositions(row: Int): List<GridPosition> {
        require(row in SIZE_RANGE) { "Row $row is out of bounds" }
        return (0 until GRID_SIZE).map { col -> GridPosition(Row(row), Col(col)) }
    }

    /**
     * Returns all positions in a column.
     */
    fun getColumnPositions(col: Int): List<GridPosition> {
        require(col in SIZE_RANGE) { "Column $col is out of bounds" }
        return (0 until GRID_SIZE).map { row -> GridPosition(Row(row), Col(col)) }
    }

    companion object {
        val SIZE_RANGE = 0 until GameConfig.GRID_SIZE
        const val GRID_SIZE = GameConfig.GRID_SIZE
        const val GRID_ROWS = GameConfig.GRID_ROWS
        const val GRID_COLS = GameConfig.GRID_COLS
    }
}

/**
 * Represents the current status of the game.
 */
@Serializable
enum class GameStatus {
    Playing,
    Paused,
    GameOver
}
