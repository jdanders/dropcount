package io.github.jdanders.dropseven.model

/**
 * Represents the complete state of the game.
 */
data class GameState(
    val grid: List<List<Cell>> = List(GRID_SIZE) { List(GRID_SIZE) { Cell.Empty } },
    val nextDisc: Disc = Disc.Numbered(1),
    val score: Int = 0,
    val totalDrops: Int = 0,
    val dropsUntilNewRow: Int = 30,
    val baseDropsPerRow: Int = 30, // Tracks the "target" for next row (30→29→28...)
    val mode: GameMode = GameMode.Normal,
    val status: GameStatus = GameStatus.Playing,
    val currentChain: Int = 0,
    val level: Int = 1,
    val gameOverDiscs: Map<Int, Disc> = emptyMap() // Column -> Disc that was pushed out
) {
    companion object {
        const val GRID_SIZE = 7
        const val GRID_ROWS = 7
        const val GRID_COLS = 7
    }
    
    /**
     * Gets the cell at the specified position.
     */
    fun getCell(row: Int, col: Int): Cell {
        require(row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
            "Position ($row, $col) is out of bounds"
        }
        return grid[row][col]
    }
    
    /**
     * Returns a new GameState with the cell at the specified position updated.
     */
    fun setCell(row: Int, col: Int, cell: Cell): GameState {
        require(row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
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
     * Checks if a column is full (top row is occupied).
     */
    fun isColumnFull(col: Int): Boolean {
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        return grid[0][col].isOccupied()
    }
    
    /**
     * Gets the first empty row in a column (from bottom to top).
     * Returns null if the column is full.
     */
    fun getFirstEmptyRow(col: Int): Int? {
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
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
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        return grid[row].count { it.isOccupied() }
    }
    
    /**
     * Counts the number of discs in a specific column.
     */
    fun countDiscsInColumn(col: Int): Int {
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        return grid.count { it[col].isOccupied() }
    }
    
    /**
     * Counts contiguous (adjacent) discs in a row starting from a position.
     * Counts left and right until hitting a gap or edge.
     */
    fun countContiguousDiscsInRow(row: Int, col: Int): Int {
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        
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
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        
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
    fun getContiguousRegionInRow(row: Int, col: Int): Set<Pair<Int, Int>> {
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        
        if (!grid[row][col].isOccupied()) return emptySet()
        
        val positions = mutableSetOf<Pair<Int, Int>>()
        
        // Find left boundary
        var c = col
        while (c >= 0 && grid[row][c].isOccupied()) {
            positions.add(row to c)
            c--
        }
        
        // Find right boundary
        c = col + 1
        while (c < GRID_SIZE && grid[row][c].isOccupied()) {
            positions.add(row to c)
            c++
        }
        
        return positions
    }
    
    /**
     * Gets all positions in the contiguous region for a given position in a column.
     */
    fun getContiguousRegionInColumn(row: Int, col: Int): Set<Pair<Int, Int>> {
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        
        if (!grid[row][col].isOccupied()) return emptySet()
        
        val positions = mutableSetOf<Pair<Int, Int>>()
        
        // Find top boundary
        var r = row
        while (r >= 0 && grid[r][col].isOccupied()) {
            positions.add(r to col)
            r--
        }
        
        // Find bottom boundary
        r = row + 1
        while (r < GRID_SIZE && grid[r][col].isOccupied()) {
            positions.add(r to col)
            r++
        }
        
        return positions
    }
    
    /**
     * Returns all positions in a row.
     */
    fun getRowPositions(row: Int): List<Pair<Int, Int>> {
        require(row in 0 until GRID_SIZE) { "Row $row is out of bounds" }
        return (0 until GRID_SIZE).map { col -> row to col }
    }
    
    /**
     * Returns all positions in a column.
     */
    fun getColumnPositions(col: Int): List<Pair<Int, Int>> {
        require(col in 0 until GRID_SIZE) { "Column $col is out of bounds" }
        return (0 until GRID_SIZE).map { row -> row to col }
    }
}

/**
 * Represents the current status of the game.
 */
enum class GameStatus {
    Playing,
    Paused,
    GameOver
}

