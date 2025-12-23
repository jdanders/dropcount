package io.github.jdanders.dropseven.engine

import io.github.jdanders.dropseven.model.*

/**
 * Core game engine that handles all game logic.
 */
class GameEngine(private val discGenerator: DiscGenerator) {
    
    /**
     * Drops a disc in the specified column.
     * Returns the result with all animation steps.
     */
    fun dropDiscWithSteps(state: GameState, column: Int): DropResult {
        android.util.Log.d("Drop7", "========================================")
        android.util.Log.d("Drop7", "DROP DISC in column $column, disc=${state.nextDisc}")
        
        // Check if column is full
        if (state.isColumnFull(column)) {
            android.util.Log.d("Drop7", "Column $column is FULL - Game Over")
            return DropResult(emptyList(), state.copy(status = GameStatus.GameOver))
        }
        
        // Find the first empty row in the column
        val targetRow = state.getFirstEmptyRow(column) ?: return DropResult(emptyList(), state.copy(status = GameStatus.GameOver))
        android.util.Log.d("Drop7", "Disc will land at row $targetRow")
        
        // Place the disc
        var newState = state.setCell(targetRow, column, Cell.Occupied(state.nextDisc))
        
        // Increment drop counter
        newState = newState.copy(
            totalDrops = newState.totalDrops + 1,
            dropsUntilNewRow = newState.dropsUntilNewRow - 1
        )
        
        // Generate next disc
        newState = newState.copy(nextDisc = discGenerator.generateNextDisc())
        
        // Process all chain reactions and collect steps
        val chainSteps = processChainReactionsWithSteps(newState)
        newState = chainSteps.lastOrNull()?.stateAfterRemoval ?: newState
        
        // Check if we need to add a new row
        if (newState.dropsUntilNewRow <= 0) {
            android.util.Log.d("Drop7", "Adding new row and checking for matches...")
            val stateWithNewRow = addNewRow(newState)
            val newRowSteps = processChainReactionsWithSteps(stateWithNewRow)
            
            android.util.Log.d("Drop7", "New row created ${newRowSteps.size} chain steps")
            
            // Mark the first step after new row addition
            val markedSteps = newRowSteps.mapIndexed { index, step ->
                if (index == 0) {
                    android.util.Log.d("Drop7", "Marking step 0 as first after new row")
                    step.copy(isFirstStepAfterNewRow = true)
                } else {
                    step
                }
            }
            
            val finalState = markedSteps.lastOrNull()?.stateAfterRemoval ?: stateWithNewRow
            val totalSteps = chainSteps.size + markedSteps.size
            android.util.Log.d("Drop7", "Returning DropResult with $totalSteps total steps (${chainSteps.size} before row + ${markedSteps.size} after row)")
            return DropResult(chainSteps + markedSteps, finalState)
        }
        
        android.util.Log.d("Drop7", "Returning DropResult with ${chainSteps.size} steps (no new row)")
        return DropResult(chainSteps, newState)
    }
    
    /**
     * Drops a disc in the specified column.
     * Returns the new game state after the disc is dropped and all chains are resolved.
     * (Legacy method for tests)
     */
    fun dropDisc(state: GameState, column: Int): GameState {
        return dropDiscWithSteps(state, column).finalState
    }
    
    /**
     * Processes all chain reactions and returns steps for animation.
     */
    private fun processChainReactionsWithSteps(state: GameState): List<ChainStep> {
        val steps = mutableListOf<ChainStep>()
        var currentState = state
        var chainLevel = 0
        var hasMatches = true
        
        while (hasMatches) {
            val matchPositions = findMatches(currentState)
            
            if (matchPositions.isEmpty()) {
                hasMatches = false
            } else {
                chainLevel++
                
                val stateBeforeRemoval = currentState
                
                // Find all positions in the contiguous regions that contain matches
                val highlightPositions = mutableSetOf<Pair<Int, Int>>()
                for ((row, col) in matchPositions) {
                    // Add all discs in the contiguous row region
                    highlightPositions.addAll(currentState.getContiguousRegionInRow(row, col))
                    // Add all discs in the contiguous column region
                    highlightPositions.addAll(currentState.getContiguousRegionInColumn(row, col))
                }
                
                // Remove matched discs and crack adjacent solid discs
                currentState = removeMatches(currentState, matchPositions, chainLevel)
                
                // Apply gravity
                currentState = applyGravity(currentState)
                
                // Add step with both before and after states
                steps.add(ChainStep(stateBeforeRemoval, matchPositions, highlightPositions, currentState, chainLevel))
            }
        }
        return steps
    }
    
    /**
     * Processes all chain reactions until no more matches are found.
     * (Legacy method for tests)
     */
    private fun processChainReactions(state: GameState): GameState {
        val steps = processChainReactionsWithSteps(state)
        return steps.lastOrNull()?.stateAfterRemoval ?: state.copy(currentChain = 0)
    }
    
    /**
     * Finds all disc positions that should break based on row/column counts.
     */
    private fun findMatches(state: GameState): Set<Pair<Int, Int>> {
        val matches = mutableSetOf<Pair<Int, Int>>()
        
        android.util.Log.d("Drop7", "=== CHECKING FOR MATCHES ===")
        
        // First, print the entire grid state
        android.util.Log.d("Drop7", "Current grid state:")
        for (row in 0 until GameState.GRID_SIZE) {
            val rowStr = (0 until GameState.GRID_SIZE).map { col ->
                val cell = state.getCell(row, col)
                when {
                    cell is Cell.Occupied && cell.disc is Disc.Numbered -> (cell.disc as Disc.Numbered).value.toString()
                    cell is Cell.Occupied && cell.disc is Disc.Solid -> "S${(cell.disc as Disc.Solid).cracks}"
                    else -> "."
                }
            }.joinToString(" ")
            android.util.Log.d("Drop7", "Row $row: $rowStr")
        }
        
        // Now check for matches
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                val cell = state.getCell(row, col)
                if (cell is Cell.Occupied) {
                    val disc = cell.disc
                    if (disc is Disc.Numbered) {
                        // Use CONTIGUOUS counting, not total counting!
                        val rowCount = state.countContiguousDiscsInRow(row, col)
                        val colCount = state.countContiguousDiscsInColumn(row, col)
                        
                        // Count what types are in the row and column
                        val rowDiscs = (0 until GameState.GRID_SIZE).map { c ->
                            val cell = state.getCell(row, c)
                            when {
                                cell is Cell.Occupied && cell.disc is Disc.Numbered -> "N${(cell.disc as Disc.Numbered).value}"
                                cell is Cell.Occupied && cell.disc is Disc.Solid -> "S"
                                else -> "."
                            }
                        }.joinToString(",")
                        
                        val colDiscs = (0 until GameState.GRID_SIZE).map { r ->
                            val cell = state.getCell(r, col)
                            when {
                                cell is Cell.Occupied && cell.disc is Disc.Numbered -> "N${(cell.disc as Disc.Numbered).value}"
                                cell is Cell.Occupied && cell.disc is Disc.Solid -> "S"
                                else -> "."
                            }
                        }.joinToString(",")
                        
                        android.util.Log.d("Drop7", "Numbered disc at ($row,$col) value=${disc.value}")
                        android.util.Log.d("Drop7", "  Row $row discs: [$rowDiscs] CONTIGUOUS count=$rowCount")
                        android.util.Log.d("Drop7", "  Col $col discs: [$colDiscs] CONTIGUOUS count=$colCount")
                        
                        if (rowCount == disc.value || colCount == disc.value) {
                            android.util.Log.d("Drop7", "  *** MATCH! (contiguous rowCount=$rowCount, colCount=$colCount, value=${disc.value}) ***")
                            matches.add(row to col)
                        }
                    }
                }
            }
        }
        
        android.util.Log.d("Drop7", "Total matches found: ${matches.size}")
        return matches
    }
    
    /**
     * Removes matched discs and cracks adjacent solid discs.
     */
    private fun removeMatches(
        state: GameState,
        matchPositions: Set<Pair<Int, Int>>,
        chainLevel: Int
    ): GameState {
        var newState = state
        
        // Calculate score for this chain
        val basePoints = matchPositions.size * 7
        val chainMultiplier = if (chainLevel > 1) chainLevel else 1
        val scoreGain = basePoints * chainMultiplier
        newState = newState.copy(
            score = newState.score + scoreGain,
            currentChain = chainLevel
        )
        
        // Remove matched discs
        matchPositions.forEach { (row, col) ->
            newState = newState.setCell(row, col, Cell.Empty)
        }
        
        // Crack adjacent solid discs
        val adjacentPositions = matchPositions.flatMap { (row, col) ->
            getAdjacentPositions(row, col)
        }.toSet()
        
        adjacentPositions.forEach { (row, col) ->
            val cell = newState.getCell(row, col)
            if (cell is Cell.Occupied && cell.disc is Disc.Solid) {
                val solid = cell.disc as Disc.Solid
                val crackedDisc = solid.addCrack()
                newState = newState.setCell(row, col, Cell.Occupied(crackedDisc))
            }
        }
        
        return newState
    }
    
    /**
     * Gets all adjacent positions (up, down, left, right) for a given position.
     */
    private fun getAdjacentPositions(row: Int, col: Int): List<Pair<Int, Int>> {
        val adjacent = mutableListOf<Pair<Int, Int>>()
        
        // Up
        if (row > 0) adjacent.add(row - 1 to col)
        // Down
        if (row < GameState.GRID_SIZE - 1) adjacent.add(row + 1 to col)
        // Left
        if (col > 0) adjacent.add(row to col - 1)
        // Right
        if (col < GameState.GRID_SIZE - 1) adjacent.add(row to col + 1)
        
        return adjacent
    }
    
    /**
     * Applies gravity to make discs fall down into empty spaces.
     */
    private fun applyGravity(state: GameState): GameState {
        var newState = state
        
        android.util.Log.d("Drop7", "--- Applying gravity ---")
        
        // Process each column
        for (col in 0 until GameState.GRID_SIZE) {
            val discsInColumn = mutableListOf<Disc>()
            
            // Collect all discs in the column from top to bottom
            for (row in 0 until GameState.GRID_SIZE) {
                val cell = newState.getCell(row, col)
                if (cell is Cell.Occupied) {
                    discsInColumn.add(cell.disc)
                }
            }
            
            // Clear the column
            for (row in 0 until GameState.GRID_SIZE) {
                newState = newState.setCell(row, col, Cell.Empty)
            }
            
            // Place discs from bottom up, maintaining their top-to-bottom order
            // The last disc in the list should be at the bottom
            discsInColumn.forEachIndexed { index, disc ->
                val row = GameState.GRID_SIZE - discsInColumn.size + index
                newState = newState.setCell(row, col, Cell.Occupied(disc))
            }
        }
        
        return newState
    }
    
    /**
     * Adds a new row from the bottom, pushing all existing discs up.
     */
    private fun addNewRow(state: GameState): GameState {
        android.util.Log.d("Drop7", "=== ADDING NEW ROW ===")
        
        // Shift all rows UP (each row gets content from row below it)
        // Start by checking what would be pushed out of the grid
        val discsPushedOut = mutableMapOf<Int, Disc>() // column -> disc that was in row 0
        for (col in 0 until GameState.GRID_SIZE) {
            val topCell = state.getCell(0, col)
            if (topCell is Cell.Occupied) {
                discsPushedOut[col] = topCell.disc
            }
        }
        
        // If any discs would be pushed out, it's game over
        if (discsPushedOut.isNotEmpty()) {
            android.util.Log.d("Drop7", "Discs pushed out of grid - GAME OVER at columns: ${discsPushedOut.keys}")
            return state.copy(
                status = GameStatus.GameOver,
                gameOverDiscs = discsPushedOut
            )
        }
        
        val newGrid = MutableList(GameState.GRID_SIZE) { row ->
            MutableList(GameState.GRID_SIZE) { col ->
                if (row == GameState.GRID_SIZE - 1) {
                    // Bottom row will be filled with new discs
                    Cell.Empty
                } else {
                    // Each row gets the content from the row BELOW (pushing UP)
                    state.getCell(row + 1, col)
                }
            }
        }
        
        // Generate new bottom row (always all solid discs)
        val newRowDiscs = discGenerator.generateNewRow()
        android.util.Log.d("Drop7", "Generated new bottom row: ${newRowDiscs.map { disc -> 
            if (disc is Disc.Solid) "S${disc.cracks}" else (disc as Disc.Numbered).value.toString()
        }.joinToString(" ")}")
        
        for (col in 0 until GameState.GRID_SIZE) {
            newGrid[GameState.GRID_SIZE - 1][col] = Cell.Occupied(newRowDiscs[col])
        }
        
        // Update drops until next row based on game mode
        val rowConfig = RowTimingConfig.fromGameMode(state.mode)
        val nextBaseDropsPerRow = if (rowConfig.shouldDecrement) {
            maxOf(state.baseDropsPerRow - 1, rowConfig.minDropsUntilRow)
        } else {
            rowConfig.initialDropsUntilRow
        }
        
        android.util.Log.d("Drop7", "Next drops until new row: $nextBaseDropsPerRow (was: ${state.baseDropsPerRow})")
        
        val newState = state.copy(
            grid = newGrid,
            dropsUntilNewRow = nextBaseDropsPerRow,
            baseDropsPerRow = nextBaseDropsPerRow,
            level = state.level + 1
        )
        
        // Don't process chains here - let the caller handle it so animations work
        android.util.Log.d("Drop7", "Returning state with new row (chains will be processed by caller)")
        return newState
    }
    
    /**
     * Starts a new game with the specified mode.
     */
    fun startNewGame(mode: GameMode): GameState {
        val rowConfig = RowTimingConfig.fromGameMode(mode)
        return GameState(
            mode = mode,
            nextDisc = discGenerator.generateNextDisc(),
            dropsUntilNewRow = rowConfig.initialDropsUntilRow,
            baseDropsPerRow = rowConfig.initialDropsUntilRow,
            status = GameStatus.Playing
        )
    }
}

