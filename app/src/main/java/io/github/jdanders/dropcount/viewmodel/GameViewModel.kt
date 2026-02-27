package io.github.jdanders.dropcount.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jdanders.dropcount.config.AnimationConfig
import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.data.AllGameStatistics
import io.github.jdanders.dropcount.data.GameResult
import io.github.jdanders.dropcount.data.ModeGameStatistics
import io.github.jdanders.dropcount.data.PreferencesManager
import io.github.jdanders.dropcount.data.ScoreRepository
import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.AnimationState
import io.github.jdanders.dropcount.model.ChallengeDifficulty
import io.github.jdanders.dropcount.model.GameMode
import io.github.jdanders.dropcount.model.GameState
import io.github.jdanders.dropcount.model.GameStatus
import io.github.jdanders.dropcount.model.AnimationSpeed
import io.github.jdanders.dropcount.model.GridPosition
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.viewmodel.UIState
import io.github.jdanders.dropcount.viewmodel.AnimationData
import io.github.jdanders.dropcount.util.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import io.github.jdanders.dropcount.data.DataStoreGameStateRepository
import io.github.jdanders.dropcount.data.GameStateRepository

/**
 * ViewModel for the game that manages game state and handles user actions.
 */
class GameViewModel(
    application: Application,
    private val gameStateRepository: GameStateRepository
) : AndroidViewModel(application) {

    @Suppress("unused") // Used by ViewModelProvider
    constructor(application: Application) : this(
        application,
        DataStoreGameStateRepository(application)
    )
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _animationData = MutableStateFlow(AnimationData())
    val animationData: StateFlow<AnimationData> = _animationData.asStateFlow()

    // Exposed individual state flows for backwards compatibility
    val animationState: StateFlow<AnimationState> = _animationData.map { it.state }.stateIn(viewModelScope, SharingStarted.Eagerly, AnimationState.Idle)
    val hoveredColumn: StateFlow<Int?> = _uiState.map { it.hoveredColumn }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val floatingPoints: StateFlow<Map<GridPosition, Int>> = _uiState.map { it.floatingPoints }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val levelUpBonus: StateFlow<Int?> = _uiState.map { it.levelUpBonus }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val canUndo: StateFlow<Boolean> = _uiState.map { it.canUndo }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val animationSpeed: StateFlow<AnimationSpeed> = _animationData.map { it.speed }.stateIn(viewModelScope, SharingStarted.Eagerly, AnimationSpeed.MEDIUM)

    private val _visualTheme = MutableStateFlow(VisualTheme.CLASSIC)
    val visualTheme: StateFlow<VisualTheme> = _visualTheme.asStateFlow()

    private val _hasSeenTutorial = MutableStateFlow(true) // Default to true until loaded
    val hasSeenTutorial: StateFlow<Boolean> = _hasSeenTutorial.asStateFlow()

    private var gameEngine: GameEngine? = null
    private val preferencesManager = PreferencesManager(application)
    private val scoreRepository = ScoreRepository(preferencesManager)
    // gameStateRepository is now injected via constructor

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    // Single mutex to prevent concurrent animations
    private val animationMutex = Mutex()

    private val _allGameStatistics = MutableStateFlow(AllGameStatistics())
    val allGameStatistics: StateFlow<AllGameStatistics> = _allGameStatistics.asStateFlow()

    // Undo history - stores previous game states
    private val undoHistory = mutableListOf<GameState>()

    // Store statistics from BEFORE the current game ends (for display on game over screen)
    private val _statisticsBeforeGameOver = MutableStateFlow<ModeGameStatistics?>(null)
    val statisticsBeforeGameOver: StateFlow<ModeGameStatistics?> = _statisticsBeforeGameOver.asStateFlow()

    /**
     * Derived state: is any animation in progress?
     */
    val isAnimating: StateFlow<Boolean> = animationState
        .map { it !is AnimationState.Idle }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Gets statistics for the current game mode.
     */
    val currentModeStatistics: StateFlow<ModeGameStatistics> = combine(
        _gameState,
        _allGameStatistics,
        _statisticsBeforeGameOver
    ) { state, allStats, beforeStats ->
        // If game is over and we have "before" stats, use those for display
        if (state.status == GameStatus.GameOver && beforeStats != null) {
            beforeStats
        } else {
            allStats.forMode(state.mode)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ModeGameStatistics())

    init {
        // Try to load saved game state first
        viewModelScope.launch {
            Logger.d("GameViewModel", "Init: Attempting to load saved game state")
            val savedState = gameStateRepository.load()
            Logger.d("GameViewModel", "Init: Loaded state is ${if (savedState != null) "non-null" else "null"}, status=${savedState?.status}")

            if (savedState != null && (savedState.status == GameStatus.Playing || savedState.status == GameStatus.Paused)) {
                Logger.d("GameViewModel", "Restored saved game state: score=${savedState.score}, level=${savedState.level}, mode=${savedState.mode}")
                _gameState.value = savedState

                // Recreate game engine with the saved mode
                val discGenerator = DiscGenerator(savedState.mode)
                discGenerator.setRecentDiscs(savedState.recentDiscs)
                gameEngine = GameEngine(discGenerator)

                // Load high score for this mode
                scoreRepository.getHighScore(savedState.mode).collect { highScore ->
                    _highScore.value = highScore
                }
            } else {
                Logger.d("GameViewModel", "No valid saved state found, starting new game")
                // No saved state or game wasn't playing - start fresh
                startNewGame(GameMode.Normal)
            }
        }

        // Load statistics once and keep them synced
        viewModelScope.launch {
            preferencesManager.gameStatistics.collect { stats ->
                Logger.d("GameViewModel", "Statistics updated: normalMode.totalGamesPlayed=${stats.normalMode.totalGamesPlayed}")
                _allGameStatistics.value = stats
            }
        }

        // Load animation speed setting
        viewModelScope.launch {
            preferencesManager.animationSpeed.collect { speed: AnimationSpeed ->
                _animationData.value = _animationData.value.copy(speed = speed)
            }
        }

        // Load visual theme setting
        viewModelScope.launch {
            preferencesManager.visualTheme.collect { theme ->
                _visualTheme.value = theme
            }
        }

        // Load hasSeenTutorial setting
        viewModelScope.launch {
            preferencesManager.hasSeenTutorial.collect { hasSeen ->
                _hasSeenTutorial.value = hasSeen
            }
        }

        // Auto-save game state when animations complete
        viewModelScope.launch {
            combine(_gameState, animationState) { state, animState ->
                state to animState
            }.collect { (state, animState) ->
                // Save when not animating and game is playing or paused
                if (animState is AnimationState.Idle && (state.status == GameStatus.Playing || state.status == GameStatus.Paused)) {
                    gameStateRepository.save(state)
                } else if (state.status == GameStatus.GameOver) {
                    // Clear saved state when game ends
                    gameStateRepository.clear()
                }
            }
        }
    }

    /**
     * Marks the tutorial as seen.
     */
    fun dismissTutorial() {
        viewModelScope.launch {
            preferencesManager.setHasSeenTutorial(true)
        }
    }

    /**
     * Starts a new game with the specified mode.
     */
    fun startNewGame(mode: GameMode) {
        // Determine seed: deterministic for Sequence mode, random for others
        val seedForNewGame = when (mode) {
            is GameMode.Sequence -> mode.seed
            else -> System.currentTimeMillis()
        }

        // Create and configure disc generator with the seed before any generation
        val discGenerator = DiscGenerator(mode)
        discGenerator.setSeed(seedForNewGame)

        // Create engine and generate new game state
        gameEngine = GameEngine(discGenerator)
        val newGameState = gameEngine?.startNewGame(mode) ?: GameState()

        // Store the game state with the seed used to generate it
        _gameState.value = newGameState.copy(randomSeed = seedForNewGame)
        _animationData.value = _animationData.value.copy(state = AnimationState.Idle)

        // Clear undo history
        undoHistory.clear()
        _uiState.value = _uiState.value.copy(canUndo = false)

        // Clear "before game over" statistics
        _statisticsBeforeGameOver.value = null

        // Clear saved game state since we're starting fresh
        viewModelScope.launch {
            gameStateRepository.clear()
        }

        // Load high score for this mode
        viewModelScope.launch {
            scoreRepository.getHighScore(mode).collect { highScore ->
                _highScore.value = highScore
            }
        }
    }

    /**
     * Drops a disc in the specified column.
     * This is the ONLY place animations are triggered - UI just displays them.
     */
    fun dropDisc(column: Int) {
        // Quick rejection without acquiring lock
        val stateBeforeDrop = _gameState.value
        if (stateBeforeDrop.status != GameStatus.Playing || _animationData.value.state !is AnimationState.Idle) {
            Logger.d("GameViewModel", "Drop rejected: game not playing or animation in progress")
            return
        }

        viewModelScope.launch {
            animationMutex.withLock {
                try {
                    val engine = gameEngine ?: return@withLock

                    // Calculate seed progression: increment deterministically for reproducibility
                    val currentSeed = stateBeforeDrop.randomSeed
                    val nextSeed = currentSeed + 1

                    // Configure generator to use current seed before drop (for nextDisc generation)
                    engine.getDiscGenerator().setSeed(currentSeed)

                    // Save state for undo (with the seed that will regenerate the current nextDisc)
                    saveStateToUndoHistory(stateBeforeDrop)

                    // Check if column is full
                    val targetRow = stateBeforeDrop.getFirstEmptyRow(column)
                    val isColumnFull = targetRow == null

                    // Animation 1: Drop disc from preview to target position
                    Logger.d("GameViewModel", "Starting drop animation to ${if (isColumnFull) "game over position" else "row $targetRow"}")
                    _animationData.value = _animationData.value.copy(state = AnimationState.DroppingDisc(
                        disc = stateBeforeDrop.nextDisc,
                        column = column,
                        targetRow = targetRow ?: -1  // -1 means game over position
                    ))
                    delay(AnimationConfig.getDropDuration(_animationData.value.speed))

                    // If column was full, trigger game over and stop
                    if (isColumnFull) {
                        Logger.d("GameViewModel", "Column full - game over")
                        val dropResult = engine.dropDiscWithSteps(stateBeforeDrop, column)
                        _gameState.value = dropResult.finalState
                        _animationData.value = _animationData.value.copy(state = AnimationState.Idle)

                        if (dropResult.finalState.status == GameStatus.GameOver) {
                            // Capture statistics BEFORE saving the new game result
                            captureStatisticsBeforeGameOver(dropResult.finalState)
                            saveGameStatistics(dropResult.finalState)
                        }
                        return@withLock
                    }

                    // Process the drop and get animation steps
                    val dropResult = engine.dropDiscWithSteps(stateBeforeDrop, column)
                    Logger.d("GameViewModel", "Received DropResult: ${dropResult.steps.size} steps before row, ${dropResult.stepsAfterNewRow.size} steps after row")

                    // Update final state with incremented seed for next drop
                    val stateAfterDrop = dropResult.finalState.copy(randomSeed = nextSeed)

                    // Process steps BEFORE new row
                    for ((index, step) in dropResult.steps.withIndex()) {
                        processAnimationStep(step, index)
                    }

                    // Return to Idle after processing steps
                    _animationData.value = _animationData.value.copy(state = AnimationState.Idle)

                    // Animate new row addition if needed
                    if (dropResult.stateBeforeNewRow != null) {
                        Logger.d("GameViewModel", "Animating new row addition")

                        // Safety: Use final state if no steps after new row
                        val stateWithNewRow = if (dropResult.stepsAfterNewRow.isNotEmpty()) {
                            dropResult.stepsAfterNewRow.first().stateBeforeRemoval
                        } else {
                            dropResult.finalState
                        }

                        // Calculate level bonus based on game mode
                        val levelBonus = GameConfig.getLevelBonus(stateBeforeDrop.mode)

                        // Show level-up animation
                        _uiState.value = _uiState.value.copy(levelUpBonus = levelBonus)

                        _animationData.value = _animationData.value.copy(state = AnimationState.AddingNewRow(
                            stateBeforeNewRow = dropResult.stateBeforeNewRow,
                            stateAfterNewRow = stateWithNewRow
                        ))

                        // Wait for animation to complete
                        delay(AnimationConfig.getNewRowDisplayDuration(_animationData.value.speed))

                        // After animation completes, update gameState and return to Idle
                        // This order is important: update state first, then clear animation
                        _gameState.value = stateWithNewRow
                        _animationData.value = _animationData.value.copy(state = AnimationState.Idle)
                    }

                    // Process steps AFTER new row
                    for ((index, step) in dropResult.stepsAfterNewRow.withIndex()) {
                        processAnimationStep(step, index)
                    }

                    // Return to final state and idle
                    _gameState.value = stateAfterDrop
                    _animationData.value = _animationData.value.copy(state = AnimationState.Idle)
                    _uiState.value = _uiState.value.copy(floatingPoints = emptyMap(), levelUpBonus = null)

                    Logger.d("GameViewModel", "All animations complete, seed updated to $nextSeed")

                    // Save statistics if game over
                    if (stateAfterDrop.status == GameStatus.GameOver) {
                        // Capture statistics BEFORE saving the new game result
                        captureStatisticsBeforeGameOver(stateAfterDrop)
                        saveGameStatistics(stateAfterDrop)
                    }
                } catch (e: Exception) {
                    Logger.e("GameViewModel", "Error in dropDisc", e)
                    _animationData.value = _animationData.value.copy(state = AnimationState.Idle)
                    _uiState.value = _uiState.value.copy(floatingPoints = emptyMap(), levelUpBonus = null)
                }
            }
        }
    }

    /**
     * Processes a single animation step from a chain reaction.
     *
     * IMPORTANT: The delay() durations must match the animation durations
     * in GameGrid.kt to ensure smooth transitions. If you change animation
     * durations in GameConfig, update both places.
     */
    private suspend fun processAnimationStep(step: io.github.jdanders.dropcount.model.ChainStep, index: Int) {
        try {
            Logger.d("GameViewModel", "Step $index: chain=${step.chainLevel}, matches=${step.matchPositions.size}")

            // Animation: Highlight matches
            _gameState.value = step.stateBeforeRemoval
            _animationData.value = _animationData.value.copy(state = AnimationState.HighlightingMatches(
                positions = step.highlightPositions,
                colors = step.highlightColors,
                chainLevel = step.chainLevel
            ))
            _uiState.value = _uiState.value.copy(floatingPoints = step.discPointValues)
            delay(AnimationConfig.getHighlightDuration(_animationData.value.speed))

            // Animation: Apply gravity
            _uiState.value = _uiState.value.copy(floatingPoints = emptyMap())
            _gameState.value = step.stateAfterRemoval
            _animationData.value = _animationData.value.copy(state = AnimationState.ApplyingGravity(step.gravityMovements))

            delay(AnimationConfig.getGravityDuration(_animationData.value.speed, step.gravityMovements.isNotEmpty()))
        } catch (e: Exception) {
            Logger.e("GameViewModel", "Error in animation step $index", e)
            // Ensure we always return to Idle on error
            _animationData.value = _animationData.value.copy(state = AnimationState.Idle)
            _uiState.value = _uiState.value.copy(floatingPoints = emptyMap())
            throw e
        }
    }

    /**
     * Pauses the game.
     */
    fun pauseGame() {
        _gameState.value = _gameState.value.copy(status = GameStatus.Paused)
    }

    /**
     * Resumes the game.
     */
    fun resumeGame() {
        _gameState.value = _gameState.value.copy(status = GameStatus.Playing)
    }

    /**
     * Restarts the current game.
     */
    fun restartGame() {
        val currentMode = _gameState.value.mode
        startNewGame(currentMode)
    }

    /**
     * Sets the currently hovered column for preview.
     */
    fun setHoveredColumn(column: Int?) {
        _uiState.value = _uiState.value.copy(hoveredColumn = column)
    }

    /**
     * Sets the animation speed.
     */
    fun setAnimationSpeed(speed: AnimationSpeed) {
        viewModelScope.launch {
            preferencesManager.setAnimationSpeed(speed)
        }
    }

    /**
     * Calculates the target row for a disc drop in the specified column.
     */
    fun getTargetRowForColumn(column: Int): Int? {
        return _gameState.value.getFirstEmptyRow(column)
    }

    /**
     * Undoes the last move by restoring the previous game state.
     * Restores both the game state AND the random generator state, ensuring that
     * redoing the same move will generate identical discs.
     */
    fun undoLastMove() {
        val currentStatus = _gameState.value.status
        val isGameOver = currentStatus == GameStatus.GameOver

        if (undoHistory.isEmpty() || _animationData.value.state !is AnimationState.Idle) {
            return
        }

        val restoredState = undoHistory.removeLastOrNull()
        if (restoredState != null) {
            _gameState.value = restoredState
            _uiState.value = _uiState.value.copy(canUndo = undoHistory.isNotEmpty())

            gameEngine?.getDiscGenerator()?.apply {
                setSeed(restoredState.randomSeed)
                setRecentDiscs(restoredState.recentDiscs)
            }

            if (isGameOver) {
                Logger.d("GameViewModel", "Undo from GameOver: restored to score=${restoredState.score}, nextDisc=${restoredState.nextDisc}, seed=${restoredState.randomSeed}")
            } else {
                Logger.d("GameViewModel", "Undo: restored to score=${restoredState.score}, nextDisc=${restoredState.nextDisc}, seed=${restoredState.randomSeed}")
            }
        }
    }

    /**
     * Sets the visual theme.
     */
    fun setVisualTheme(theme: VisualTheme) {
        viewModelScope.launch {
            preferencesManager.setVisualTheme(theme)
        }
    }

    /**
     * Captures the current statistics for the game mode BEFORE the game ends.
     * This is stored separately so the game over screen shows stats before this game.
     */
    private fun captureStatisticsBeforeGameOver(finalState: GameState) {
        val currentStats = _allGameStatistics.value.forMode(finalState.mode)
        _statisticsBeforeGameOver.value = currentStats
        Logger.d("GameViewModel", "Captured statistics before game over: totalGamesPlayed=${currentStats.totalGamesPlayed}, highestScore=${currentStats.highestScore}")
    }

    /**
     * Saves game statistics when a game ends.
     */
    private suspend fun saveGameStatistics(finalState: GameState) {
        Logger.d("GameViewModel", "Game Over - saving statistics")

        try {
            scoreRepository.saveHighScore(finalState.mode, finalState.score)

            val gameResult = GameResult(
                score = finalState.score,
                mode = finalState.mode,
                level = finalState.level,
                longestChain = finalState.longestChain,
                highestSingleMove = finalState.highestSingleScore,
                totalDrops = finalState.totalDrops
            )

            preferencesManager.addGameResult(gameResult)
            Logger.d("GameViewModel", "Statistics saved successfully")

        } catch (e: Exception) {
            Logger.e("GameViewModel", "Error saving game statistics", e)
        }
    }

    /**
     * Saves the current game state to undo history.
     * Captures the state before any modifications occur, including the nextDisc
     * and the randomSeed needed to regenerate it.
     */
    private fun saveStateToUndoHistory(state: GameState) {
        if (state.status != GameStatus.Playing) {
            return
        }

        undoHistory.add(state)

        while (undoHistory.size > GameConfig.MAX_UNDO_HISTORY) {
            undoHistory.removeAt(0)
        }

        _uiState.value = _uiState.value.copy(canUndo = true)
        Logger.d("GameViewModel", "Saved to undo history (${undoHistory.size}/${GameConfig.MAX_UNDO_HISTORY}), nextDisc=${state.nextDisc}, seed=${state.randomSeed}")
    }
}
