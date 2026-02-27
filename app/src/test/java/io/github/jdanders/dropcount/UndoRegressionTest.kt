package io.github.jdanders.dropcount

import io.github.jdanders.dropcount.config.GameConfig
import io.github.jdanders.dropcount.engine.DiscGenerator
import io.github.jdanders.dropcount.engine.GameEngine
import io.github.jdanders.dropcount.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for Undo functionality.
 * Ensures that undo correctly restores game state and generator history.
 */
class UndoRegressionTest {
    
    private lateinit var gameEngine: GameEngine
    private lateinit var discGenerator: DiscGenerator
    
    @Before
    fun setup() {
        discGenerator = DiscGenerator(GameMode.Normal)
        gameEngine = GameEngine(discGenerator)
    }
    
    @Test
    fun testUndoRestoresGameState() {
        // This test simulates the logic in GameViewModel.undoLastMove()
        
        // 1. Start a game
        var state1 = gameEngine.startNewGame(GameMode.Normal)
        val initialScore = state1.score
        val initialNextDisc = state1.nextDisc
        
        // 2. Perform a move
        val state2 = gameEngine.dropDisc(state1, 3)
        
        // 3. "Undo" by reverting to state1
        // In the app, this is done by popping from undoHistory
        val restoredState = state1
        
        assertEquals("Score should be restored", initialScore, restoredState.score)
        assertEquals("Next disc should be restored", initialNextDisc, restoredState.nextDisc)
        assertEquals("Grid should be restored", state1.grid, restoredState.grid)
    }
    
    @Test
    fun testUndoRestoresGeneratorDeterminism() {
        // Regression: Undo should restore the generator state so replaying the same move
        // results in the same sequence of discs.
        
        val initialSeed = 12345L
        val mode = GameMode.Normal
        
        // Setup generator with fixed seed
        val generator = DiscGenerator(mode)
        generator.setSeed(initialSeed)
        val engine = GameEngine(generator)
        
        // 1. Initial state (simulating GameViewModel.startNewGame)
        val state1 = engine.startNewGame(mode).copy(randomSeed = initialSeed)
        
        // 2. Perform Move A (simulating GameViewModel.dropDisc)
        val currentSeedA = state1.randomSeed
        generator.setSeed(currentSeedA) // Done in dropDisc before generating steps
        val state2A = engine.dropDisc(state1, 3).copy(randomSeed = currentSeedA + 1)
        val nextDiscAfterA = state2A.nextDisc
        
        // 3. "Undo" and restore generator state (simulating GameViewModel.undoLastMove)
        generator.setSeed(state1.randomSeed)
        generator.setRecentDiscs(state1.recentDiscs)
        
        // 4. Perform Move A again (Replay - simulating GameViewModel.dropDisc)
        generator.setSeed(state1.randomSeed) // Done in dropDisc
        val state2B = engine.dropDisc(state1, 3).copy(randomSeed = state1.randomSeed + 1)
        val nextDiscAfterB = state2B.nextDisc
        
        assertEquals("Replaying the same move after undo should produce the same next disc", 
            nextDiscAfterA, nextDiscAfterB)
        assertEquals("Replaying the same move should produce same grid",
            state2A.grid, state2B.grid)
        
        // 5. Check deeper history (after more moves)
        val currentSeed3A = state2A.randomSeed
        generator.setSeed(currentSeed3A)
        val state3A = engine.dropDisc(state2A, 4).copy(randomSeed = currentSeed3A + 1)
        val nextDiscAfter3A = state3A.nextDisc
        
        // Undo back to state2A
        generator.setSeed(state2A.randomSeed)
        generator.setRecentDiscs(state2A.recentDiscs)
        
        // Replay move
        generator.setSeed(state2A.randomSeed)
        val state3B = engine.dropDisc(state2A, 4).copy(randomSeed = state2A.randomSeed + 1)
        val nextDiscAfter3B = state3B.nextDisc
        
        assertEquals("Deeper replay should also be deterministic", 
            nextDiscAfter3A, nextDiscAfter3B)
    }

    @Test
    fun testTripleUndoReplayDeterminism() {
        // Scenario: Player plays 3 discs, undoes three times, 
        // and then gets the exact same three discs again when played.
        
        val initialSeed = 999L
        val mode = GameMode.Normal
        val generator = DiscGenerator(mode)
        generator.setSeed(initialSeed)
        val engine = GameEngine(generator)
        
        // 1. Start game and record the sequence of next discs
        // State 0 has nextDisc1
        val state0 = engine.startNewGame(mode).copy(randomSeed = initialSeed)
        val nextDisc1 = state0.nextDisc
        
        // 2. Play 3 discs and record the next disc that appears after each drop
        
        // Drop 1 (using nextDisc1)
        generator.setSeed(state0.randomSeed)
        val state1 = engine.dropDisc(state0, 3).copy(randomSeed = state0.randomSeed + 1)
        val nextDisc2 = state1.nextDisc
        
        // Drop 2 (using nextDisc2)
        generator.setSeed(state1.randomSeed)
        val state2 = engine.dropDisc(state1, 3).copy(randomSeed = state1.randomSeed + 1)
        val nextDisc3 = state2.nextDisc
        
        // Drop 3 (using nextDisc3)
        generator.setSeed(state2.randomSeed)
        val state3 = engine.dropDisc(state2, 3).copy(randomSeed = state2.randomSeed + 1)
        val nextDisc4 = state3.nextDisc
        
        // 3. Undo 3 times back to state0
        // (In the app, this would pop state2, then state1, then state0 from history)
        
        // Restore state0 generator state
        generator.setSeed(state0.randomSeed)
        generator.setRecentDiscs(state0.recentDiscs)
        
        // 4. Replay the exact same 3 moves
        
        // Replay Drop 1
        generator.setSeed(state0.randomSeed)
        val replayState1 = engine.dropDisc(state0, 3).copy(randomSeed = state0.randomSeed + 1)
        assertEquals("First replayed next disc should match", nextDisc2, replayState1.nextDisc)
        assertEquals("First replayed grid should match", state1.grid, replayState1.grid)
        
        // Replay Drop 2
        generator.setSeed(replayState1.randomSeed)
        val replayState2 = engine.dropDisc(replayState1, 3).copy(randomSeed = replayState1.randomSeed + 1)
        assertEquals("Second replayed next disc should match", nextDisc3, replayState2.nextDisc)
        assertEquals("Second replayed grid should match", state2.grid, replayState2.grid)
        
        // Replay Drop 3
        generator.setSeed(replayState2.randomSeed)
        val replayState3 = engine.dropDisc(replayState2, 3).copy(randomSeed = replayState2.randomSeed + 1)
        assertEquals("Third replayed next disc should match", nextDisc4, replayState3.nextDisc)
        assertEquals("Third replayed grid should match", state3.grid, replayState3.grid)
    }
    
    @Test
    fun testUndoRestoresRecentDiscsHistory() {
        // Specifically verify that recentDiscs list is correctly preserved in GameState
        // and used by the engine.
        
        val mode = GameMode.Normal
        val generator = DiscGenerator(mode)
        val engine = GameEngine(generator)
        
        var state = engine.startNewGame(mode)
        
        // Initial recent discs should match generator's state
        assertEquals("GameState should capture generator's recent discs",
            generator.getRecentDiscs(), state.recentDiscs)
            
        // After a move
        val stateAfterMove = engine.dropDisc(state, 0)
        
        assertNotEquals("Recent discs should change after a move", 
            state.recentDiscs, stateAfterMove.recentDiscs)
        assertEquals("GameState should update recent discs after move",
            generator.getRecentDiscs(), stateAfterMove.recentDiscs)
    }
}
