package io.github.jdanders.dropcount.engine

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.config.ScoringFormula
import io.github.jdanders.dropcount.model.*
import io.github.jdanders.dropcount.util.Logger
import kotlin.math.roundToInt

/**
 * Core game engine that handles all game logic.
 */
class GameEngine(private val discGenerator: DiscGenerator) {

    /**
     * Drops a disc in the specified column.
     * Returns the result with all animation steps.
     */
    fun dropDiscWithSteps(state: GameState, column: Int): DropResult {
        Logger.d("GameEngine", "========================================")
        Logger.d("GameEngine", "DROP DISC in column $column, disc=${state.nextDisc}")

        // Check if column is full
        if (state.isColumnFull(column)) {
            Logger.d("GameEngine", "Column $column is FULL - Game Over")
            return DropResult(
                emptyList(),
                state.copy(
                    status = GameStatus.GameOver,
                    gameOverDiscs = mapOf(column to state.nextDisc)
                ),
                null,
                emptyList()
            )
        }

        // Find the first empty row in the column
        val targetRow = state.getFirstEmptyRow(column) ?: return DropResult(emptyList(), state.copy(status = GameStatus.GameOver), null, emptyList())
        Logger.d("GameEngine", "Disc will land at row $targetRow")

        // Place the disc
        var newState = state.setCell(targetRow, column, Cell.Occupied(state.nextDisc))

        // Increment drop counter and reset chain for new drop
        newState = newState.copy(
            totalDrops = newState.totalDrops + 1,
            dropsUntilNewRow = newState.dropsUntilNewRow - 1,
            currentChain = 0
        )

        // Generate next disc
        newState = newState.copy(nextDisc = discGenerator.generateNextDisc())

        // Process all chain reactions and collect steps
        val chainSteps = processChainReactionsWithSteps(newState)
        newState = chainSteps.lastOrNull()?.stateAfterRemoval ?: newState

        // Check if we need to add a new row
        if (newState.dropsUntilNewRow <= 0) {
            Logger.d("GameEngine", "Adding new row and checking for matches...")

            // Store state BEFORE adding new row for animation
            val stateBeforeNewRow = newState
            val stateWithNewRow = addNewRow(newState)

            // If adding new row caused game over (discs pushed out), skip chain reaction processing
            if (stateWithNewRow.status == GameStatus.GameOver) {
                Logger.d("GameEngine", "New row caused game over - skipping chain reaction processing")
                return DropResult(chainSteps, stateWithNewRow, stateBeforeNewRow, emptyList())
            }

            val newRowSteps = processChainReactionsWithSteps(stateWithNewRow)

            Logger.d("GameEngine", "New row created ${newRowSteps.size} chain steps")

            // Mark the first step after new row addition
            val markedSteps = newRowSteps.mapIndexed { index, step ->
                if (index == 0) {
                    Logger.d("GameEngine", "Marking step 0 as first after new row")
                    step.copy(isFirstStepAfterNewRow = true)
                } else {
                    step
                }
            }

            val finalStateBeforeFullCheck = markedSteps.lastOrNull()?.stateAfterRemoval ?: stateWithNewRow
            val finalState = if (finalStateBeforeFullCheck.status == GameStatus.Playing && finalStateBeforeFullCheck.isGridFull()) {
                Logger.d("GameEngine", "Grid is entirely full - Game Over")
                finalStateBeforeFullCheck.copy(status = GameStatus.GameOver)
            } else {
                finalStateBeforeFullCheck
            }

            val totalSteps = chainSteps.size + markedSteps.size
            Logger.d("GameEngine", "Returning DropResult with $totalSteps total steps (${chainSteps.size} before row + ${markedSteps.size} after row)")
            return DropResult(chainSteps, finalState, stateBeforeNewRow, markedSteps)
        }

        Logger.d("GameEngine", "Returning DropResult with ${chainSteps.size} steps (no new row)")
        val finalState = if (newState.status == GameStatus.Playing && newState.isGridFull()) {
            Logger.d("GameEngine", "Grid is entirely full - Game Over")
            newState.copy(status = GameStatus.GameOver, recentDiscs = discGenerator.getRecentDiscs())
        } else {
            newState.copy(recentDiscs = discGenerator.getRecentDiscs())
        }
        return DropResult(chainSteps, finalState, null, emptyList())
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
     * Gets the disc generator (for restoring random state during undo).
     */
    fun getDiscGenerator(): DiscGenerator = discGenerator

    /**
     * Processes all chain reactions and returns steps for animation.
     */
    private fun processChainReactionsWithSteps(state: GameState): List<ChainStep> {
        val steps = mutableListOf<ChainStep>()
        var currentState = state
        var chainLevel = state.currentChain
        var hasMatches = true

        while (hasMatches) {
            val matchInfos = findMatchesWithInfo(currentState)

            if (matchInfos.isEmpty()) {
                hasMatches = false
            } else {
                chainLevel++

                val stateBeforeRemoval = currentState
                val matchPositions = matchInfos.map { it.position }.toSet()

                // Build highlight positions and colors based on which direction caused each match
                val highlightPositions = mutableSetOf<GridPosition>()
                val highlightColors = mutableMapOf<GridPosition, Int>()

                for (matchInfo in matchInfos) {
                    val pos = matchInfo.position
                    val row = pos.row.value
                    val col = pos.col.value
                    val discValue = matchInfo.discValue

                    // Only add the region(s) that actually caused the match
                    if (matchInfo.matchedInRow) {
                        val rowRegion = currentState.getContiguousRegionInRow(row, col)
                        highlightPositions.addAll(rowRegion)
                        rowRegion.forEach { pos -> highlightColors[pos] = discValue }
                    }
                    if (matchInfo.matchedInColumn) {
                        val colRegion = currentState.getContiguousRegionInColumn(row, col)
                        highlightPositions.addAll(colRegion)
                        colRegion.forEach { pos -> highlightColors[pos] = discValue }
                    }
                }

                // Calculate points for each disappearing disc
                val pointsPerDisc = calculateScoreGain(chainLevel)
                val discPointValues = matchPositions.associateWith { pointsPerDisc }

                // Remove matched discs and crack adjacent solid discs
                currentState = removeMatches(currentState, matchPositions, chainLevel)

                // Apply gravity and track movements
                val gravityResult = applyGravityWithMovements(currentState)
                currentState = gravityResult.newState

                // Add step with both before and after states
                steps.add(ChainStep(stateBeforeRemoval, matchPositions, highlightPositions, highlightColors, currentState, gravityResult.movements, chainLevel, discPointValues = discPointValues))
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
     * Match information including position and which direction(s) caused the match.
     */
    private data class MatchInfo(
        val position: GridPosition,
        val discValue: Int,
        val matchedInRow: Boolean,
        val matchedInColumn: Boolean
    )

    /**
     * Finds all disc positions that should break based on row/column counts.
     * Returns detailed match information including which direction caused the match.
     */
    private fun findMatchesWithInfo(state: GameState): List<MatchInfo> {
        val matches = mutableListOf<MatchInfo>()

        Logger.d("GameEngine", "=== CHECKING FOR MATCHES ===")

        // First, print the entire grid state
        Logger.d("GameEngine", "Current grid state:")
        for (row in 0 until GameState.GRID_SIZE) {
            val rowStr = (0 until GameState.GRID_SIZE).map { col ->
                val cell = state.getCell(row, col)
                val disc = cell.discOrNull()
                when (disc) {
                    is Disc.Numbered -> disc.value.toString()
                    is Disc.Solid -> "S${disc.cracks}"
                    else -> "."
                }
            }.joinToString(" ")
            Logger.d("GameEngine", "Row $row: $rowStr")
        }

        // Now check for matches
        for (row in 0 until GameState.GRID_SIZE) {
            for (col in 0 until GameState.GRID_SIZE) {
                val cell = state.getCell(row, col)
                if (cell.isOccupied()) {
                    val disc = cell.discOrNull()!!
                    if (disc is Disc.Numbered) {
                        // Use CONTIGUOUS counting, not total counting!
                        val rowCount = state.countContiguousDiscsInRow(row, col)
                        val colCount = state.countContiguousDiscsInColumn(row, col)

                        val matchedInRow = rowCount == disc.value
                        val matchedInColumn = colCount == disc.value

                        if (matchedInRow || matchedInColumn) {
                            Logger.d("GameEngine", "  *** MATCH at ($row,$col) value=${disc.value}, row=$matchedInRow, col=$matchedInColumn ***")
                            matches.add(MatchInfo(GridPosition(Row(row), Col(col)), disc.value, matchedInRow, matchedInColumn))
                        }
                    }
                }
            }
        }

        Logger.d("GameEngine", "Total matches found: ${matches.size}")
        return matches
    }

    /**
     * Calculates the score gain for a specific chain multiplier step.
     * Sequence: 7, 39, 109, 224, 391...
     *
     * @param chainMultiplier The index n in the sequence.
     * @return The scoreGain as an Int.
     */
    fun calculateScoreGain(chainMultiplier: Int): Int {
        return ScoringFormula.calculateScoreGain(chainMultiplier)
    }


    /**
     * Removes matched discs and cracks adjacent solid discs.
     */
    private fun removeMatches(
        state: GameState,
        matchPositions: Set<GridPosition>,
        chainLevel: Int
    ): GameState {
        var newState = state

        // Calculate score for this chain
        val scoreGain = calculateScoreGain(chainLevel) * matchPositions.size

        // Update statistics
        val newLongestChain = maxOf(newState.longestChain, chainLevel)
        val newHighestSingleScore = maxOf(newState.highestSingleScore, scoreGain)

        newState = newState.copy(
            score = newState.score + scoreGain,
            currentChain = chainLevel,
            longestChain = newLongestChain,
            highestSingleScore = newHighestSingleScore
        )

        // Remove matched discs
        matchPositions.forEach { pos ->
            newState = newState.setCell(pos.row.value, pos.col.value, Cell.Empty)
        }

        // Crack adjacent solid discs
        // Don't use toSet() - we want duplicates so discs adjacent to multiple matches get cracked multiple times
        val adjacentPositionsList = matchPositions.flatMap { pos ->
            getAdjacentPositions(pos.row.value, pos.col.value)
        }

        // Count how many times each position appears (how many matches it's adjacent to)
        val crackCounts = adjacentPositionsList.groupingBy { it }.eachCount()

        crackCounts.forEach { (position, count) ->
            val row = position.row.value
            val col = position.col.value
            val cell = newState.getCell(row, col)
            if (cell.isOccupied() && cell.discOrNull() is Disc.Solid) {
                var currentDisc: Disc = cell.discOrNull()!!
                Logger.d("GameEngine", "Solid disc at ($row,$col) adjacent to $count match(es), applying $count crack(s)")

                // Apply cracks equal to the number of adjacent matches
                repeat(count) {
                    if (currentDisc is Disc.Solid) {
                        currentDisc = (currentDisc as Disc.Solid).addCrack()
                    }
                }

                Logger.d("GameEngine", "  After cracking: ${if (currentDisc is Disc.Numbered) "Revealed as ${(currentDisc as Disc.Numbered).value}" else "Still solid with ${(currentDisc as Disc.Solid).cracks} crack(s)"}")
                newState = newState.setCell(row, col, Cell.Occupied(currentDisc))
            }
        }

        return newState
    }

    /**
     * Gets all adjacent positions (up, down, left, right) for a given position.
     */
    private fun getAdjacentPositions(row: Int, col: Int): List<GridPosition> {
        val adjacent = mutableListOf<GridPosition>()

        // Up
        if (row > 0) adjacent.add(GridPosition(Row(row - 1), Col(col)))
        // Down
        if (row < GameState.GRID_SIZE - 1) adjacent.add(GridPosition(Row(row + 1), Col(col)))
        // Left
        if (col > 0) adjacent.add(GridPosition(Row(row), Col(col - 1)))
        // Right
        if (col < GameState.GRID_SIZE - 1) adjacent.add(GridPosition(Row(row), Col(col + 1)))

        return adjacent
    }

    /**
     * Result of applying gravity with movement tracking.
     */
    private data class GravityResult(
        val newState: GameState,
        val movements: Map<GridPosition, GridPosition> // from -> to
    )

    /**
     * Applies gravity to make discs fall down into empty spaces.
     * Returns the new state and a map of movements (from position -> to position).
     */
    private fun applyGravityWithMovements(state: GameState): GravityResult {
        var newState = state
        val movements = mutableMapOf<GridPosition, GridPosition>()

        Logger.d("GameEngine", "--- Applying gravity ---")

        // Process each column
        for (col in 0 until GameState.GRID_SIZE) {
            val discsWithPositions = mutableListOf<Pair<Int, Disc>>() // (originalRow, disc)

            // Collect all discs in the column from top to bottom
            for (row in 0 until GameState.GRID_SIZE) {
                val cell = newState.getCell(row, col)
                if (cell.isOccupied()) {
                    discsWithPositions.add(row to cell.discOrNull()!!)
                }
            }

            // Clear the column
            for (row in 0 until GameState.GRID_SIZE) {
                newState = newState.setCell(row, col, Cell.Empty)
            }

            // Place discs from bottom up, maintaining their top-to-bottom order
            // The last disc in the list should be at the bottom
            discsWithPositions.forEachIndexed { index, (originalRow, disc) ->
                val newRow = GameState.GRID_SIZE - discsWithPositions.size + index
                newState = newState.setCell(newRow, col, Cell.Occupied(disc))

                // Track movement if disc actually moved
                if (originalRow != newRow) {
                    movements[GridPosition(Row(originalRow), Col(col))] = GridPosition(Row(newRow), Col(col))
                }
            }
        }

        return GravityResult(newState, movements)
    }

    /**
     * Adds a new row from the bottom, pushing all existing discs up.
     */
    private fun addNewRow(state: GameState): GameState {
        Logger.d("GameEngine", "=== ADDING NEW ROW ===")

        // Shift all rows UP (each row gets content from row below it)
        // Start by checking what would be pushed out of the grid
        val discsPushedOut = mutableMapOf<Int, Disc>() // column -> disc that was in row 0
        for (col in 0 until GameConfig.GRID_SIZE) {
            val topCell = state.getCell(0, col)
            if (topCell.isOccupied()) {
                discsPushedOut[col] = topCell.discOrNull()!!
            }
        }

        // Create the new grid with rows shifted up (discs in row 0 are pushed out)
        val newGrid = MutableList(GameConfig.GRID_SIZE) { row ->
            MutableList(GameConfig.GRID_SIZE) { col ->
                if (row == GameConfig.GRID_SIZE - 1) {
                    // Bottom row will be filled with new discs
                    Cell.Empty
                } else {
                    // Each row gets the content from the row BELOW (pushing UP)
                    // Row 0 gets content from row 1, row 1 gets content from row 2, etc.
                    state.getCell(row + 1, col)
                }
            }
        }

        // If any discs would be pushed out, it's game over
        // But we still need to return the shifted grid state
        if (discsPushedOut.isNotEmpty()) {
            Logger.d("GameEngine", "Discs pushed out of grid - GAME OVER at columns: ${discsPushedOut.keys}")
            // Continue to create the new state with shifted grid and new row
        }

        // Generate new bottom row (always all solid discs)
        val newRowDiscs = discGenerator.generateNewRow()
        Logger.d("GameEngine", "Generated new bottom row: ${newRowDiscs.map { disc ->
            if (disc is Disc.Solid) "S${disc.cracks}" else (disc as Disc.Numbered).value.toString()
        }.joinToString(" ")}")

        for (col in 0 until GameConfig.GRID_SIZE) {
            newGrid[GameConfig.GRID_SIZE - 1][col] = Cell.Occupied(newRowDiscs[col])
        }

        // Update drops until next row based on game mode
        val rowConfig = RowTimingConfig.fromGameMode(state.mode)
        val nextBaseDropsPerRow = if (rowConfig.shouldDecrement) {
            maxOf(state.baseDropsPerRow - 1, rowConfig.minDropsUntilRow)
        } else {
            rowConfig.initialDropsUntilRow
        }

        Logger.d("GameEngine", "Next drops until new row: $nextBaseDropsPerRow (was: ${state.baseDropsPerRow})")

        // Award level bonus points based on game mode
        val levelBonus = GameConfig.getLevelBonus(state.mode)
        Logger.d("GameEngine", "Level up! Awarding $levelBonus bonus points")

        val newState = state.copy(
            grid = newGrid,
            dropsUntilNewRow = nextBaseDropsPerRow,
            baseDropsPerRow = nextBaseDropsPerRow,
            level = state.level + 1,
            score = state.score + levelBonus,
            currentChain = state.currentChain,  // Preserve currentChain for chain continuation
            status = if (discsPushedOut.isNotEmpty()) GameStatus.GameOver else state.status,
            gameOverDiscs = discsPushedOut,
            recentDiscs = discGenerator.getRecentDiscs()
        )

        // Don't process chains here - let the caller handle it so animations work
        if (discsPushedOut.isNotEmpty()) {
            Logger.d("GameEngine", "Returning state with new row and game over (discs pushed out)")
        } else {
            Logger.d("GameEngine", "Returning state with new row (chains will be processed by caller)")
        }
        return newState
    }

    /**
     * Starts a new game with the specified mode.
     * Populates the grid with 8-14 random discs, with max 4 per column.
     */
    fun startNewGame(mode: GameMode): GameState {
        val rowConfig = RowTimingConfig.fromGameMode(mode)

        // Special handling for Sequence mode - deterministic initial setup
        if (mode is GameMode.Sequence) {
            Logger.d("GameEngine", "Starting Sequence mode with deterministic setup")

            // Create empty grid
            val emptyGrid = MutableList(GameConfig.GRID_SIZE) { MutableList<Cell>(GameConfig.GRID_SIZE) { Cell.Empty } }

            // Add one row of solid discs at the bottom
            val bottomRow = discGenerator.generateNewRow()
            for (col in 0 until GameConfig.GRID_SIZE) {
                emptyGrid[GameConfig.GRID_SIZE - 1][col] = Cell.Occupied(bottomRow[col])
            }

            return GameState(
                grid = emptyGrid,
                mode = mode,
                nextDisc = discGenerator.generateNextDisc(),
                dropsUntilNewRow = rowConfig.initialDropsUntilRow,
                baseDropsPerRow = rowConfig.initialDropsUntilRow,
                status = GameStatus.Playing,
                recentDiscs = discGenerator.getRecentDiscs()
            )
        }

        // Determine target number of final discs (after match resolution)
        val minDiscs = GameConfig.getInitialMinDiscs(mode)
        val maxDiscs = GameConfig.getInitialMaxDiscs(mode)
        val targetDiscCount = (minDiscs..maxDiscs).random()
        Logger.d("GameEngine", "Starting new game with target of $targetDiscCount final discs (mode: $mode)")

        // Create empty grid
        var grid = MutableList(GameConfig.GRID_SIZE) { MutableList<Cell>(GameConfig.GRID_SIZE) { Cell.Empty } }

        // Keep adding discs and resolving matches until we reach the target count
        var totalAttempts = 0
        val maxTotalAttempts = GameConfig.MAX_STARTUP_ATTEMPTS

        while (totalAttempts < maxTotalAttempts) {
            // Create current game state
            var gameState = GameState(
                grid = grid,
                mode = mode,
                nextDisc = discGenerator.generateNextDisc(),
                dropsUntilNewRow = rowConfig.initialDropsUntilRow,
                baseDropsPerRow = rowConfig.initialDropsUntilRow,
                status = GameStatus.Playing,
                recentDiscs = discGenerator.getRecentDiscs()
            )

            // Count current discs
            val currentDiscCount = gameState.grid.sumOf { row -> row.count { it.isOccupied() } }

            if (currentDiscCount >= targetDiscCount) {
                // For Challenge mode, ensure we have at least one solid disc
                if (mode is GameMode.Challenge && grid.none { row -> row.any { it.discOrNull() is Disc.Solid } }) {
                    // Replace one random disc with a solid one
                    val occupiedPositions = mutableListOf<GridPosition>()
                    for (r in 0 until GameConfig.GRID_SIZE) {
                        for (c in 0 until GameConfig.GRID_SIZE) {
                            if (grid[r][c].isOccupied()) occupiedPositions.add(GridPosition(Row(r), Col(c)))
                        }
                    }
                    if (occupiedPositions.isNotEmpty()) {
                        val pos = occupiedPositions.random()
                        grid[pos.row.value][pos.col.value] = Cell.Occupied(discGenerator.generateNewRow().first()) // generateNewRow returns solids
                    }
                }
                
                Logger.d("GameEngine", "Reached target disc count: $currentDiscCount")
                return gameState.copy(grid = grid, recentDiscs = discGenerator.getRecentDiscs())
            }

            // Need to add another disc
            totalAttempts++

            // Pick random column
            val col = (0 until GameConfig.GRID_SIZE).random()

            // Check if column can accept another disc
            val discsInColumn = gameState.grid.count { it[col].isOccupied() }
            if (discsInColumn < GameConfig.INITIAL_MAX_DISCS_PER_COLUMN) {
                // Find bottom-most empty row in this column
                val row = GameConfig.GRID_SIZE - 1 - discsInColumn

                // Generate disc according to game mode rules (bypass run constraints for initial setup)
                val disc = discGenerator.generateInitialDisc()

                // Place disc temporarily to check for matches
                grid[row][col] = Cell.Occupied(disc)
                val tempState = GameState(
                    grid = grid,
                    mode = mode,
                    nextDisc = discGenerator.generateNextDisc(),
                    dropsUntilNewRow = rowConfig.initialDropsUntilRow,
                    baseDropsPerRow = rowConfig.initialDropsUntilRow,
                    status = GameStatus.Playing,
                    recentDiscs = discGenerator.getRecentDiscs()
                )

                // Process any matches that were created
                val matchInfos = findMatchesWithInfo(tempState)
                if (matchInfos.isNotEmpty()) {
                    Logger.d("GameEngine", "Found matches with new disc, resolving them")
                    val resolvedState = processChainReactions(tempState)

                    // Reset all solid discs to uncracked state for initial setup
                    val cleanedGrid = resolvedState.grid.map { row ->
                        row.map { cell ->
                            val disc = cell.discOrNull()
                            if (disc is Disc.Solid) {
                                Cell.Occupied(disc.copy(cracks = GameConfig.SOLID_DISC_INITIAL_CRACKS))
                            } else {
                                cell
                            }
                        }
                    }

                    grid = cleanedGrid.map { it.toMutableList() }.toMutableList()
                } else {
                    // No matches, keep the disc
                    Logger.d("GameEngine", "Placed disc at ($row, $col): $disc (no matches)")
                }
            }
        }

        Logger.d("GameEngine", "Failed to reach target disc count after $maxTotalAttempts attempts")
        // Return what we have
        return GameState(
            grid = grid,
            mode = mode,
            nextDisc = discGenerator.generateNextDisc(),
            dropsUntilNewRow = rowConfig.initialDropsUntilRow,
            baseDropsPerRow = rowConfig.initialDropsUntilRow,
            status = GameStatus.Playing,
            recentDiscs = discGenerator.getRecentDiscs()
        )
    }
}
