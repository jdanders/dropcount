package io.github.jdanders.dropseven.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jdanders.dropseven.data.PreferencesManager
import io.github.jdanders.dropseven.data.ScoreRepository
import io.github.jdanders.dropseven.engine.DiscGenerator
import io.github.jdanders.dropseven.engine.GameEngine
import io.github.jdanders.dropseven.model.AnimationState
import io.github.jdanders.dropseven.model.GameMode
import io.github.jdanders.dropseven.model.GameState
import io.github.jdanders.dropseven.model.GameStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the game that manages game state and handles user actions.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _animationState = MutableStateFlow<AnimationState>(AnimationState.Idle)
    val animationState: StateFlow<AnimationState> = _animationState.asStateFlow()
    
    private val _highlightedPositions = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    val highlightedPositions: StateFlow<Set<Pair<Int, Int>>> = _highlightedPositions.asStateFlow()
    
    private var gameEngine: GameEngine? = null
    private val preferencesManager = PreferencesManager(application)
    private val scoreRepository = ScoreRepository(preferencesManager)
    
    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()
    
    private var isAnimating = false
    
    init {
        // Start with Normal mode by default
        startNewGame(GameMode.Normal)
    }
    
    /**
     * Starts a new game with the specified mode.
     */
    fun startNewGame(mode: GameMode) {
        val discGenerator = DiscGenerator(mode)
        gameEngine = GameEngine(discGenerator)
        _gameState.value = gameEngine?.startNewGame(mode) ?: GameState()
        
        // Load high score for this mode
        viewModelScope.launch {
            scoreRepository.getHighScore(mode).collect { highScore ->
                _highScore.value = highScore
            }
        }
    }
    
    /**
     * Drops a disc in the specified column.
     */
    fun dropDisc(column: Int) {
        viewModelScope.launch {
            val currentState = _gameState.value
            
            // Don't allow drops if game is not in playing state or if already animating
            if (currentState.status != GameStatus.Playing || isAnimating) {
                return@launch
            }
            
            isAnimating = true
            
            gameEngine?.let { engine ->
                val dropResult = engine.dropDiscWithSteps(currentState, column)
                
                android.util.Log.d("Drop7Animation", "Received DropResult with ${dropResult.steps.size} steps")
                
                // If no steps (no matches), just update to final state
                if (dropResult.steps.isEmpty()) {
                    android.util.Log.d("Drop7Animation", "No animation steps, updating directly to final state")
                    _gameState.value = dropResult.finalState
                    isAnimating = false
                    return@launch
                }
                
                android.util.Log.d("Drop7Animation", "Starting animation loop for ${dropResult.steps.size} steps")
                // Animate through each chain step
                for ((index, step) in dropResult.steps.withIndex()) {
                    android.util.Log.d("Drop7Animation", "Step $index: isFirstAfterNewRow=${step.isFirstStepAfterNewRow}, matches=${step.matchPositions.size}, highlights=${step.highlightPositions.size}")
                    
                    // If this is the first match after a new row, show the board with new row first
                    if (step.isFirstStepAfterNewRow) {
                        android.util.Log.d("Drop7Animation", "Showing new row for 500ms before highlighting")
                        // Show the board with the new row for a moment before highlighting matches
                        _gameState.value = step.stateBeforeRemoval
                        _highlightedPositions.value = emptySet()
                        delay(500) // Let user see the new row appear
                    }
                    
                    android.util.Log.d("Drop7Animation", "Highlighting ${step.highlightPositions.size} positions")
                    // Show state with entire contiguous regions highlighted
                    _gameState.value = step.stateBeforeRemoval
                    _highlightedPositions.value = step.highlightPositions
                    _animationState.value = AnimationState.HighlightMatches(step.highlightPositions, step.chainLevel)
                    delay(600) // Highlight for 600ms
                    
                    android.util.Log.d("Drop7Animation", "Removing matches and applying gravity")
                    // Clear highlights and show state after removal (with gravity applied)
                    _highlightedPositions.value = emptySet()
                    _animationState.value = AnimationState.DroppingDiscs(emptyMap())
                    _gameState.value = step.stateAfterRemoval
                    delay(400) // Show falling animation for 400ms
                }
                
                // Set final state (should already be the last stateAfterRemoval, but ensure it's set)
                _gameState.value = dropResult.finalState
                _animationState.value = AnimationState.Idle
                _highlightedPositions.value = emptySet()
                isAnimating = false
                
                // Save high score if game is over
                if (dropResult.finalState.status == GameStatus.GameOver) {
                    scoreRepository.saveHighScore(dropResult.finalState.mode, dropResult.finalState.score)
                }
            }
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
}

